/**
*  Copyright 2015 SmartThings
*
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License. You may obtain a copy of the License at:
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*  for the specific language governing permissions and limitations under the License.
*
*  PurpleAir Air Quality Station
*
*  Author: SmartThings
*
*  Date: 2018-07-04
*
*	Updates by Barry A. Burke (storageanarchy@gmail.com)
*	Date: 2017 - 2018
*
*	1.0.00 - Initial Release
*	1.0.01 - Cleanup of descriptionTexts & bug fixes
*	1.0.02 - Fixed typos
*	1.0.03 - More string edits
*	1.0.04 - Updated icons & color handling
*	1.0.05 - Now use BigDecimal for maximum precision
*	1.0.06 - Finalized conversion to BigDecimal
*	1.0.07 - Better error handling
*	1.0.08 - Changed all numeric attributes to "number"
*	1.0.09 - Changed to maintain and display only integer AQI (decimals are distracting)
*	1.0.10 - Fixed room/thing tile display
*	1.0.11 - Handles Inside PurpleAir Sensor (only 1 sensor by design)
*	1.0.12 - Internal cleanup of Inside sensor support, added runEvery3Minutes
*	1.0.13 - Code annotations for hubitat users
*	1.0.14 - Added CAQI calculation for new "Air Quality Sensor" - see https://en.wikipedia.org/wiki/Air_quality_index#CAQI
*	1.1.01 - Added automatic support for both SmartThings and Hubitat
*	1.1.02a- Fix null response handling
*	1.1.03 - Fixed descriptionText:
*   1.1.04 - Fixed incorrect collection of temperature, humidity and pressure where both sensors are not available
*	1.1.05 - Added optional debug logging preference setting
*	1.1.06 - Optimized temp/humidity/pressure updates
*	1.1.07 - Fixed Flagged sensors, added Hidden device support (needs owners's Key)
*	1.1.08 - Added reference adjustments for Temp, Humidity & Pressure
*
*/
import groovy.json.JsonSlurper
import java.math.BigDecimal

def getVersionNum() { return "1.1.08" }
private def getVersionLabel() { return "PurpleAir Air Quality Station, version ${getVersionNum()}" }


// **************************************************************************************************************************
// SmartThings/Hubitat Portability Library (SHPL)
// Copyright (c) 2019, Barry A. Burke (storageanarchy@gmail.com)
//
// The following 3 calls are safe to use anywhere within a Device Handler or Application
//  - these can be called (e.g., if (getPlatform() == 'SmartThings'), or referenced (i.e., if (platform == 'Hubitat') )
//  - performance of the non-native platform is horrendous, so it is best to use these only in the metadata{} section of a
//    Device Handler or Application
//
private String  getPlatform() { (physicalgraph?.device?.HubAction ? 'SmartThings' : 'Hubitat') }	// if (platform == 'SmartThings') ...
private Boolean getIsST()     { (physicalgraph?.device?.HubAction ? true : false) }					// if (isST) ...
private Boolean getIsHE()     { (hubitat?.device?.HubAction ? true : false) }						// if (isHE) ...
//
// The following 3 calls are ONLY for use within the Device Handler or Application runtime
//  - they will throw an error at compile time if used within metadata, usually complaining that "state" is not defined
//  - getHubPlatform() ***MUST*** be called from the installed() method, then use "state.hubPlatform" elsewhere
//  - "if (state.isST)" is more efficient than "if (isSTHub)"
//
private String getHubPlatform() {
    if (state?.hubPlatform == null) {
        state.hubPlatform = getPlatform()						// if (hubPlatform == 'Hubitat') ... or if (state.hubPlatform == 'SmartThings')...
        state.isST = state.hubPlatform.startsWith('S')			// if (state.isST) ...
        state.isHE = state.hubPlatform.startsWith('H')			// if (state.isHE) ...
    }
    return state.hubPlatform
}
private Boolean getIsSTHub() { (state.isST) }					// if (isSTHub) ...
private Boolean getIsHEHub() { (state.isHE) }					// if (isHEHub) ...
//
// **************************************************************************************************************************

metadata {
    definition (name: "PurpleAir Air Quality Station", namespace: "sandood", author: "sandood",
			    importUrl: "https://raw.githubusercontent.com/SANdood/PurpleAirStation/master/devicetypes/sandood/purpleair-air-quality-station.src/purpleair-air-quality-station.groovy") {
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Signal Strength"
        capability "Sensor"
        capability "Refresh"
        if (isST) {capability "Air Quality Sensor"} else {attribute "airQuality", "number"}

        attribute "locationName", "string"
        attribute "ID", "string"
        attribute "pressure", "number"
		attribute "airQuality", 'number'
        attribute "airQualityIndex", "string"
        attribute "aqi", "number"				// current AQI
		attribute "aqiDisplay", 'string'
		attribute "aqi10", "number"				// 10 minute average
		attribute "aqi30", "number"				// 30 minute average
		attribute "aqi1", "number"				// 1 hour average
		attribute "aqi6", "number"				// 6 hour average
		attribute "aqi24", "number"				// 24 hour average
		attribute "aqi7", "number"				// 7 day average
		attribute "pm", "number"				// current 2.5 PM (particulate matter)
		attribute "pm10", "number"				// 10 minute average
		attribute "pm30", "number"				// 30 minute average
		attribute "pm1", "number"				// 1 hour average
		attribute "pm6", "number"				// 6 hour average
		attribute "pm24", "number"				// 24 hour average
		attribute "pm7", "number"				// 7 day average
		attribute "rssi", "string"				// Signal Strength attribute (not supporting lqi)
        attribute 'message', 'string'
  		attribute "updated", "string"
        attribute "timestamp", "string"
		attribute "temperatureDisplay", 'string'
		attribute "pressure", 'number'
		attribute "pressureDisplay", 'string'
        command "refresh"
    }

    preferences {
		input(name: "purpleID", type: "text", title: (isHE?'<b>':'') + "PurpleAir Station ID" + (isHE?'</b>':''), required: true, displayDuringSetup: true, description: 'Enter the desired PurpleAir Station ID')
		input(name: "purpleKey", type: "password", title: (isHE?'<b>':'') + "PurpleAir Private Key (optional)" + (isHE?'</b>':''), required: false, displayDuringSetup: true, description: "Enter the Private Key for this Station")
    	input(name: 'updateMins', type: 'enum', description: "Select the update frequency", 
        	  title: (isHE?'<b>':'') + "Update frequency (minutes)" + (isHE?'</b>':''), displayDuringSetup: true, defaultValue: '5', options: ['1','3','5','10','15','30'], required: true)
		input "referenceTemp", "decimal", title: (isHE?'<b>':'') + "Reference temperature" + (isHE?'</b>':''), description: "Enter current reference temperature reading", displayDuringSetup: false
		input "referenceRH", "number", title: (isHE?'<b>':'') + "Reference relative humidity" + (isHE?'</b>':''), description: "Enter current reference RH% reading", displayDuringSetup: false
		input "referenceInHg", "decimal", title: (isHE?'<b>':'') + "Reference barometric pressure" + (isHE?'</b>':''), description: "Enter current reference InHg reading", displayDuringSetup: false
		input(name: 'debugOn', type: 'bool', title: (isHE?'<b>':'') + "Enable debug logging?" + (isHE?'</b>':''), displayDuringSetup: true, defaultValue: false)
    }
    
    tiles(scale: 2) {
        multiAttributeTile(name:"airQualityIndex", type:"generic", width:6, height:4, canChangeIcon: false) {
            tileAttribute("device.airQualityIndex", key: "PRIMARY_CONTROL") {
                attributeState("airQualityIndex", label:'${currentValue}', defaultState: true, 
					backgroundColors: (aqiColors)
				)
			}
            tileAttribute("device.message", key: "SECONDARY_CONTROL" ) {
				attributeState('default', label: '${currentValue}', defaultState: true, icon: "https://raw.githubusercontent.com/SANdood/PurpleAirStation/master/images/purpleair.png")
			}
        }
        valueTile('caqi', 'device.CAQI', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: 'CAQI\n${currentValue}', unit: "CAQI", 
            	backgroundColors: (caqiColors)
        }
		valueTile('aqi', 'device.aqi', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: 'AQI\n${currentValue}', icon: "https://raw.githubusercontent.com/SANdood/PurpleAirStation/master/images/purpleair-small.png",
            	backgroundColors: (aqiColors)
        }
        valueTile('aqiDisplay', 'device.aqiDisplay', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: '${currentValue}', icon: "https://raw.githubusercontent.com/SANdood/PurpleAirStation/master/images/purpleair-small.png"
        }
		valueTile('aqi10', 'device.aqi10', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: 'AQI\n${currentValue}',
            	backgroundColors: (aqiColors)
        }
		valueTile('aqi30', 'device.aqi30', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: 'AQI\n${currentValue}',
            	backgroundColors: (aqiColors)
        }
		valueTile('aqi1', 'device.aqi1', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: 'AQI\n${currentValue}',
            	backgroundColors: (aqiColors)
        }
		valueTile('aqi6', 'device.aqi6', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: 'AQI\n${currentValue}',
            	backgroundColors: (aqiColors)
        }
		valueTile('aqi24', 'device.aqi24', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: 'AQI\n${currentValue}',
            	backgroundColors: (aqiColors)
        }
		valueTile('aqi7', 'device.aqi7', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: 'AQI\n${currentValue}',
            	backgroundColors: (aqiColors)
        }
		valueTile('pm', 'device.pm', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: 'Now\n${currentValue}\nµg/m³'
        }
		valueTile('pm10', 'device.pm10', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: '10 Min\n${currentValue}\nµg/m³'
        }
		valueTile('pm30', 'device.pm30', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: '30 Min\n${currentValue}\nµg/m³'
        }
		valueTile('pm1', 'device.pm1', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: '1 Hour\n${currentValue}\nµg/m³'
        }
		valueTile('pm6', 'device.pm6', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: '6 Hour\n${currentValue}\nµg/m³'
        }
		valueTile('pm24', 'device.pm24', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: '1 Day\n${currentValue}\nµg/m³'
        }
		valueTile('pm7', 'device.pm7', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: '1 Week\n${currentValue}\nµg/m³'
        }
        valueTile("locationTile", "device.locationName", inactiveLabel: false, width: 3, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label:'${currentValue}'
        }
        standardTile("refresh", "device.weather", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: "", action: "refresh", icon:"st.secondary.refresh"
        }
        valueTile("pressure", "device.pressureDisplay", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: '${currentValue}'
        }
        valueTile("rssi", "device.rssi", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: 'RSSI\n${currentValue}db'
        }
        valueTile("ID", "device.ID", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: 'ID\n${currentValue}'
        }
		valueTile("updated", "device.updated", inactiveLabel: false, width: 3, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: 'Updated\nat ${currentValue}'
		}
        valueTile("temperature", "device.temperatureDisplay", width: 1, height: 1, canChangeIcon: true) {
            state "default", label: '${currentValue}°',
				backgroundColors:[
		            [value: 31, color: "#153591"],
		            [value: 44, color: "#1e9cbb"],
		            [value: 59, color: "#90d2a7"],
		            [value: 74, color: "#44b621"],
		            [value: 84, color: "#f1d801"],
		            [value: 95, color: "#d04e00"],
		            [value: 96, color: "#bc2323"]
            	]
        }
        valueTile("humidity", "device.humidity", decoration: "flat", width: 1, height: 1) {
			state("default", label: '${currentValue}%', unit: "%", defaultState: true, backgroundColors: [ //#d28de0")
          		[value: 10, color: "#00BFFF"],
                [value: 100, color: "#ff66ff"]
            ] )
		}
       //main(["aqiDisplay"])
        main(['airQualityIndex'])
        details([	"airQualityIndex",
					'aqi10', 'aqi30', 'aqi1', 'aqi6', 'aqi24', 'aqi7',
					'pm10', 'pm30', 'pm1', 'pm6', 'pm24', 'pm7',
					'updated', 'locationTile', 
					'temperature', 'humidity', 'pressure', 'rssi', 'ID', 'caqi', 
                    'refresh',
				])
	}
}

def noOp() {}

// parse events into attributes
def parse(String description) {
    log.debug "Parsing '${description}'"
}

def installed() {
	log.info "Installed on ${getHubPlatform()}"
	initialize()
}

def uninstalled() {
	unschedule()
}

def updated() {
	log.info "Updated with settings: ${settings}"
    state.purpleAirStation = getVersionLabel()
    state.hubPlatform = null; getHubPlatform();		// Force hub update if we are updated...just in case
    state.purpleAirVersion = getVersionLabel()
	unschedule()
    initialize()
}

def initialize() {
	log.info getVersionLabel() + " on ${getHubPlatform()} Initializing..."
	if (purpleID) {
		// Schedule the updates
		def t = updateMins ?: '5'
		if (t == '1') {
			runEvery1Minute(getPurpleAirAQI)
		} else {
			"runEvery${t}Minutes"(getPurpleAirAQI)
		}
	}
	if (debugOn) {
		log.debug "Debug logging enabled for 30 minutes"
		runIn(1800, debugOff, [overwrite: true])
	}
	state.isFlagged = 0
	
	
	// handle reference temperature / tempOffset automation
	if (settings.referenceTemp != null) {
		if (state.sensorTemp) {
			state.sensorTemp = roundIt(state.sensorTemp, 2)
			state.tempOffset = roundIt(settings.referenceTemp - state.sensorTemp, 2)
			if (debugOn) log.debug "sensorTemp: ${state.sensorTemp}, referenceTemp: ${referenceTemp}, offset: ${state.tempOffset}"
			settings.referenceTemp = null
			device.updateSetting('referenceTemp', "")
			if (isHE) device.clearSetting('referenceTemp')
			//sendEvent(getTemperatureResult(state.sensorTemp))
		} // else, preserve settings.referenceTemp, state.tempOffset will be calculate on the next temperature report
	}
	
	// handle reference humidity / RHOffset automation
	if (settings.referenceRH != null) {
		if (state.sensorRH) {
			state.sensorRH = roundIt(state.sensorRH, 0)
			state.RHOffset = roundIt(settings.referenceRH - state.sensorRH, 0)
			if (debugOn) log.debug "sensorRH: ${state.sensorRH}, referenceRH: ${referenceRH}, offset: ${state.RHOffset}"
			settings.referenceRH = null
			device.updateSetting('referenceRH', "")
			if (isHE) device.clearSetting('referenceRH')
			//sendEvent(getTemperatureResult(state.sensorTemp))
		} // else, preserve settings.referenceTemp, state.tempOffset will be calculate on the next temperature report
	}

	// handle reference barometric pressure / InHgOffset automation
	if (settings.referenceInHg != null) {
		if (state.sensorInHg) {
			state.sensorInHg = roundIt(state.sensorInHg, 2)
			state.InHgOffset = roundIt(settings.referenceInHg - state.sensorInHg, 2)
			if (debugOn) log.debug "sensorInHg: ${state.sensorInHg}, referenceInHg: ${referenceInHg}, offset: ${state.InHgOffset}"
			settings.referenceInHg = null
			device.updateSetting('referenceInHg', "")
			if (isHE) device.clearSetting('referenceInHg')
			//sendEvent(getTemperatureResult(state.sensorTemp))
		} // else, preserve settings.referenceTemp, state.tempOffset will be calculate on the next temperature report
	}
	
	sendEvent(name: 'updated', value: "", displayed: false, isStateChange: true)
    sendEvent(name: 'timestamp', value: "initializing", displayed: false, isStateChange: true)	// Send last
	state.isHidden = false
	
	getPurpleAirAQI()
    log.info "Initialization complete."
}

def runEvery3Minutes(handler) {
	Random rand = new Random()
    int randomSeconds = rand.nextInt(59)
    log.info "AQI seconds: ${randomSeconds}"
	schedule("${randomSeconds} 0/3 * * * ?", handler)
}

// handle commands
def poll() { refresh() }
def refresh() { getPurpleAirAQI() }
def configure() { updated() }

void getPurpleAirAQI() {
	if (!state.purpleAirVersion || (state.purpleAirVersion != getVersionLabel())) {
    	log.warn "Version changed, updating..."
        runIn(2, updated, [overwrite: true])
        return
    }
    if (!settings.purpleID) {
    	sendEvent(name: 'airQualityIndex', value: null, displayed: false)
		sendEvent(name: 'airQuality', value: null, displayed: false)
        sendEvent(name: 'aqi', value: null, displayed: false)
        return
    }
    def params = [
        uri: 		'https://www.purpleair.com',
        path: 		'/json',
        query: 		[show: settings?.purpleID, key: settings?.purpleKey],
		timeout:	30
        // body: ''
    ]
    // If building on/for hubitat, comment out the next line, and uncomment the one after it
	if (state.isST) {
		include 'asynchttp_v1'
    	asynchttp_v1.get(purpleAirResponse, params)
	} else {
    	asynchttpGet(purpleAirResponse, params)
	}
}

def purpleAirResponse(resp, data) {
	if (resp && (resp.status == 200)) {
		try {
			if (resp.json) {
				//log.trace "Response Status: ${resp.status}\n${resp.json}"
                logDebug("purpleAirResponse() got JSON...")
			} else {
            	// FAIL - no data
                log.warn "purpleAirResponse() no JSON: ${resp.data}"
                return false
            }
		} catch (Exception e) {
			log.error "purpleAirResponse() - General Exception: ${e}"
        	throw e
            return false
        } 
        parsePurpleAir(resp.json)
        return true
    }
    return false
}

def parsePurpleAir(response) {
	if (!response || (!response.results[0]?.Stats && !response.results[1]?.Stats)) {
		if (response && !settings.purpleKey) {
			log.warn "No data returned for PurpleAir request. Perhaps you need to enter your Private Key in Preferences?"
			return
		}
    	log.error "Invalid response for PurpleAir request: ${response}"
        return
    }
    //log.debug response
	def hidden = response.results[0].Hidden
	if (state.isHidden != hidden) state.isHidden = hidden
	logDebug("Parsing PurpleAir ${response.results[0].DEVICE_LOCATIONTYPE}${hidden?' (hidden)':''} sensor report")
    
    // Interestingly all the values in Stats are numbers, while everything else in results are strings
    def stats = [:]
    Long newest
    def single = null
    stats[0] = (response.results[0]?.Stats) ? new JsonSlurper().parseText(response.results[0].Stats) : [:]
    if (response.results[0].DEVICE_LOCATIONTYPE != 'inside') {
		stats[1] = (response.results[1]?.Stats) ? new JsonSlurper().parseText(response.results[1].Stats) : [:]
        newest = ((stats[0]?.lastModified?.toLong() > stats[1]?.lastModified?.toLong()) ? stats[0].lastModified.toLong() : stats[1].lastModified.toLong())
    } else {
    	stats[1] = [:]
        if (!response.results[0]?.A_H && (stats[0] != [:])) {
        	single = 0
            newest = stats[0]?.lastModified?.toLong()
        } else {
        	single = -1		// we only have A, and it's bad
        }
    }
    //log.debug "newest: ${newest}, timestamp: ${device.currentValue('timestamp')}"
	def timeStamp = state.isST ? device.currentValue('timestamp') : device.currentValue('timestamp', true)
    if (newest?.toString() == timeStamp) { logDebug("No update..."); return; } // nothing has changed yet
    
    Long age = now() - newest
    String oldData = ''
    if (age > 300000) {
        if	    (age > 604800000)	oldData = '1 week'
        else if (age > 172800000)	oldData = '2 days'
        else if (age > 86400000)	oldData = '1 day'
        else if (age > 43200000) 	oldData = '12 hours'
        else if (age > 3600000) 	oldData = '1 hour'
        else if (age > 300000)		oldData = '5 minutes'
        if (oldData != '') oldData = 'WARNING: No updates for more than ' + oldData
    }
    
    //log.debug stats
	if (response.results[0]?.Flag || response.results[1].Flag) {
		// One or both sensors are flagged for bad data
		def isFlagged = state?.isFlagged
		if ((isFlagged == null) || (isFlagged > 10)) isFlagged = 0
		isFlagged++;
		if (isFlagged == 1) log.warn "NOTICE: One of your sensors has been Flagged for sending bad data"
		state.isFlagged = isFlagged
			
		single = 0
		if (response.results[0]) {
			if ((!response.results[0].PM2_5Value?.toBigDecimal()) &&
				(!response.results[0].p_0_3_um?.toBigDecimal()) &&
				 (!response.results[0].p_0_5_um?.toBigDecimal()) &&
				  (!response.results[0].p_2_5_um?.toBigDecimal()) &&
				   (!response.results[0].p_5_0_um?.toBigDecimal()) &&
				    (!response.results[0].p_10_0_um?.toBigDecimal()) &&
				     (!response.results[0].pm1_0_cf_1?.toBigDecimal()) &&
					  (!response.results[0].pm2_5_cf_1?.toBigDecimal()) &&
					   (!response.results[0].pm10_0_cf_1?.toBigDecimal()) &&
						(!response.results[0].pm1_0_atm?.toBigDecimal()) &&
						 (!response.results[0].pm2_5_atm?.toBigDecimal()) &&
						 (!response.results[0].pm10_0_atm?.toBigDecimal())) {
				single = 1	// A is bad
			}
		}
		if ((single == 1) && (response.results[1])) {
			if ((!response.results[1].PM2_5Value?.toBigDecimal()) &&
				(!response.results[1].p_0_3_um?.toBigDecimal()) &&
				 (!response.results[1].p_0_5_um?.toBigDecimal()) &&
				  (!response.results[1].p_2_5_um?.toBigDecimal()) &&
				   (!response.results[1].p_5_0_um?.toBigDecimal()) &&
				    (!response.results[1].p_10_0_um?.toBigDecimal()) &&
				     (!response.results[1].pm1_0_cf_1?.toBigDecimal()) &&
					  (!response.results[1].pm2_5_cf_1?.toBigDecimal()) &&
					   (!response.results[1].pm10_0_cf_1?.toBigDecimal()) &&
						(!response.results[1].pm1_0_atm?.toBigDecimal()) &&
						 (!response.results[1].pm2_5_atm?.toBigDecimal()) &&
						 (!response.results[1].pm10_0_atm?.toBigDecimal())) {
				single = -1	// both are bad
			}
		}
	}

    if (single == null) {
        if (response.results[0]?.A_H || (stats[0]==[:])) {
            if (response.results[1]?.A_H || (stats[1]==[:])) {
                // A bad, B bad
                single = -1
            } else {
                // A bad, B good
                single = 1
            }
        } else {
            // Channel A is good
            if (response.results[1]?.A_H || (stats[1]==[:])) {
                // A good, B bad
                single = 0
            } else {
                // A good, B good
                single = 2
            }
        }
    }
	logDebug("Single: ${single}")
    def pm
    def pm10
    def pm30
    def pm1
    def pm6
    def pm24
    def pm7
    def rssi
    if (single >= 0) {
    	if (single == 2) {
        	pm   = roundIt(((stats[0].v  + stats[1].v  ) / 2.0), 2)
            pm10 = roundIt(((stats[0].v1 + stats[1].v1 ) / 2.0), 2)
            pm30 = roundIt(((stats[0].v2 + stats[1].v2 ) / 2.0), 2)
            pm1  = roundIt(((stats[0].v3 + stats[1].v3 ) / 2.0), 2)
            pm6  = roundIt(((stats[0].v4 + stats[1].v4 ) / 2.0), 2)
            pm24 = roundIt(((stats[0].v5 + stats[1].v5 ) / 2.0), 2)
            pm7  = roundIt(((stats[0].v6 + stats[1].v6 ) / 2.0), 2)
 			//if (response.results[0].RSSI?.isNumber() && response.results[1].RSSI?.isNumber()) rssi = roundIt(((response.results[0].RSSI.toBigDecimal() + response.results[1].RSSI.toBigDecimal()) / 2.0), 0)
			if (response?.results[0]?.RSSI) rssi = roundIt(response.results[0].RSSI, 0)
			if (rssi) {
				if (response?.results[1]?.RSSI) {
					rssi = roundIt((rssi + response.results[1].RSSI)/2,0)
				}
			} else if (response?.results[1]?.RSSI) rssi = roundIt(response.results[1].RSSI, 0)
											 
			//rssi = roundIt(((response.results[0]?.RSSI?.toBigDecimal() + response.results[1]?.RSSI?.toBigDecimal()) / 2.0), 0)
        } else {
        	pm   = roundIt(stats[single].v, 2)
            pm10 = roundIt(stats[single].v1, 2)
            pm30 = roundIt(stats[single].v2, 2)
            pm1  = roundIt(stats[single].v3, 2)
            pm6  = roundIt(stats[single].v4, 2)
            pm24 = roundIt(stats[single].v5, 2)
            pm7  = roundIt(stats[single].v6, 2)
            rssi = roundIt(response.results[single]?.RSSI, 0)
        }
    } else {
    	// No valid data...now what?
        oldData = "ERROR: Station ${purpleID} has no trusted sensor reports"
    }

    if (single >= 0) {
        def aqi   = roundIt(pm_to_aqi(pm), 0)
        //if (aqi < 1.0) aqi = roundIt(aqi,0)		// to avoid displaying ".4" when it should display "0.4"
        
        
        def aqi10 = roundIt(pm_to_aqi(pm10), 0)
        def aqi30 = roundIt(pm_to_aqi(pm30), 0)
        def aqi1  = roundIt(pm_to_aqi(pm1), 0)
        def aqi6  = roundIt(pm_to_aqi(pm6), 0)
        def aqi24 = roundIt(pm_to_aqi(pm24), 0)
        def aqi7  = roundIt(pm_to_aqi(pm7), 0)

        sendEvent(name: 'airQualityIndex', 	value: aqi, displayed: false)
        
        def caqi = roundIt(pm_to_caqi(pm1), 0)	// CAQI is based off of hourly data
        // sendEvent(name: "CAQI", value: caqi, unit: "CAQI", displayed: true)
		// sendEvent(name: "caqi", value: caqi, unit: "CAQI", displayed: true)
        sendEvent(name: "airQuality", value: caqi, unit: "CAQI", displayed: true, descriptionText: "The Common Air Quality Index for the hour is ${caqi} CAQI")

        String p25 = roundIt(pm,1) + ' µg/m³'
        String cond = '??'
        if (oldData == '') {
            if 		(aqi < 51)  {sendEvent(name: 'message', value: " GOOD: little to no health risk\n (${p25})", descriptionText: 'AQI is GOOD - little to no health risk'); cond = 'GOOD';}
            else if (aqi < 101) {sendEvent(name: 'message', value: " MODERATE: slight risk for some people\n (${p25})", descriptionText: 'AQI is MODERATE - slight risk for some people'); cond = 'MODERATE';}
            else if (aqi < 151) {sendEvent(name: 'message', value: " UNHEALTHY for sensitive groups\n (${p25})", descriptionText: 'AQI is UNHEALTHY for Sensitive Groups'); cond = 'UNHEALTHY';}
            else if (aqi < 201) {sendEvent(name: 'message', value: " UNHEALTHY for most people\n (${p25})", descriptionText: 'AQI is UNHEALTHY for most people'); cond = '*UNHEALTHY*';}
            else if (aqi < 301) {sendEvent(name: 'message', value: " VERY UNHEALTHY: serious effects for everyone (${p25})", descriptionText: 'AQI is VERY UNHEALTHY - serious effects for everyone'); cond = 'VERY UNHEALTHY';}
            else 				{sendEvent(name: 'message', value: " HAZARDOUS: emergency conditions for everyone (${p25})", descriptionText: 'AQI is HAZARDOUS - emergency conditions for everyone'); cond = 'HAZARDOUS';}
        } else {
            sendEvent(name: 'message', value: oldData, descriptionText: "No updates for ${roundIt((age/60000),2)} minutes")
            log.error "No updates for ${roundIt((age/60000),2)} minutes"
        }
		log.info("AQI: ${aqi}")
        
        sendEvent(name: 'aqi', 	 value: aqi,   descriptionText: "AQI real time is ${aqi}")
        sendEvent(name: 'aqiDisplay', value: "${aqi}\n${cond}", displayed: false)
        sendEvent(name: 'aqi10', value: aqi10, descriptionText: "AQI 10 minute average is ${aqi10}")
        sendEvent(name: 'aqi30', value: aqi30, descriptionText: "AQI 30 minute average is ${aqi30}")
        sendEvent(name: 'aqi1',  value: aqi1,  descriptionText: "AQI 1 hour average is ${aqi1}")
        sendEvent(name: 'aqi6',  value: aqi6,  descriptionText: "AQI 6 hour average is ${aqi6}")
        sendEvent(name: 'aqi24', value: aqi24, descriptionText: "AQI 24 hour average is ${aqi24}")
        sendEvent(name: 'aqi7',  value: aqi7,  descriptionText: "AQI 7 day average is ${aqi7}")

        sendEvent(name: 'pm',   value: pm,   unit: 'µg/m³', descriptionText: "PM2.5 real time is ${pm}µg/m³")
        sendEvent(name: 'pm10', value: pm10, unit: 'µg/m³', descriptionText: "PM2.5 10 minute average is ${pm10}µg/m³")
        sendEvent(name: 'pm30', value: pm30, unit: 'µg/m³', descriptionText: "PM2.5 30 minute average is ${pm30}µg/m³")
        sendEvent(name: 'pm1',  value: pm1,  unit: 'µg/m³', descriptionText: "PM2.5 1 hour average is ${pm1}µg/m³")
        sendEvent(name: 'pm6',  value: pm6,  unit: 'µg/m³', descriptionText: "PM2.5 6 hour average is ${pm6}µg/m³")
        sendEvent(name: 'pm24', value: pm24, unit: 'µg/m³', descriptionText: "PM2.5 24 hour average is ${pm24}µg/m³")
        sendEvent(name: 'pm7',  value: pm7,  unit: 'µg/m³', descriptionText: "PM2.5 7 day average is ${pm7}µg/m³")
    } else {
    	sendEvent(name: 'message', value: oldData) // ERROR
    }

    def temperature
    def humidity
    def pressure
 
    // Collect Temperature - may be on one, the other or both sensors
    if (response.results[0]?.temp_f?.isNumber()) {
		if (response.results[1]?.temp_f?.isNumber()) {
            temperature = roundIt((response.results[0]?.temp_f?.toBigDecimal() + response.results[1].temp_f.toBigDecimal()) / 2.0, 1)
		} else {
			temperature = roundIt(response.results[0].temp_f.toBigDecimal(), 1)
		}
	} else if (response.results[1]?.temp_f?.isNumber()){
        temperature = roundIt(response.results[1].temp_f.toBigDecimal(), 1)
	}
	// Adjust to reference temperature
	if (temperature != null) {
		if ((state.sensorTemp == null) || (state.sensorTemp != temperature)) state.sensorTemp = temperature
		if (settings.referenceTemp != null) {
			state.tempOffset = roundIt((referenceTemp - temperature), 1)
			if (debugOn) log.debug "sensorTemp: ${temperature}, referenceTemp: ${referenceTemp}, offset: ${state.tempOffset}"
			settings.referenceTemp = null
			device.updateSetting('referenceTemp', "")
			if (isHE) device.clearSetting('referenceTemp')
		}
		def offset = state.tempOffset
		if (offset == null) {
			def temp = device.currentValue('tempOffset')	// convert the old attribute to the new state variable
			offset = (temp != null) ? temp : 0.0
			state.tempOffset = offset
		}
    	if (offset != 0.0) {
    		def v = temperature
    		temperature = roundIt((v + offset), 1)
    	}
	}
	
    // Collect Humidity - may be on one, the other or both sensors
    if (response.results[0]?.humidity?.isNumber()) {
		if (response.results[1]?.humidity?.isNumber()) {
            humidity = roundIt(((response.results[0]?.humidity?.toBigDecimal() + response.results[1].humidity.toBigDecimal()) / 2.0), 0) 
		} else {
			humidity = roundIt(response.results[0].humidity.toBigDecimal(), 0)
		}
	} else if (response.results[1]?.humidity?.isNumber()) {
        humidity = roundIt(response.results[1].humidity.toBigDecimal(), 0)
	}
	// Adjust to reference humidity
	if (humidity != null) {
		if ((state.sensorRH == null) || (state.sensorRH != humidity)) state.sensorRH = humidity
		if (settings.referenceRH != null) {
			state.RHOffset = roundIt((referenceRH - humidity), 0)
			if (debugOn) log.debug "sensorRH: ${humidity}, referenceRH: ${referenceRH}, offset: ${state.RHOffset}"
			settings.referenceRH = null
			device.updateSetting('referenceRH', "")
			if (isHE) device.clearSetting('referenceRH')
		}
		def offset = state.RHOffset
		if (offset == null) {
			def RH = device.currentValue('RHOffset')	// convert the old attribute to the new state variable
			offset = (RH != null) ? RH : 0
			state.RHOffset = offset
		}
    	if (offset != 0.0) {
    		def v = humidity
    		humidity = roundIt((v + offset), 0)
    	}
	}
    // collect Pressure - may be on one, the other or both sensors
    if (response.results[0]?.pressure?.isNumber()) {
		if (response.results[1]?.pressure?.isNumber()) {
            pressure = roundIt((((response.results[0].pressure.toBigDecimal() + response.results[1].pressure.toBigDecimal()) / 2.0) * 0.02953), 2) 
		} else {
			pressure = roundIt((response.results[0].pressure.toBigDecimal() * 0.02953), 2)
		}
	} else if (response.results[1]?.pressure?.isNumber()) {
        pressure = roundIt((response.results[1].pressure.toBigDecimal() * 0.02953), 2)
	}
	// Adjust to reference pressure
	if (pressure != null) {
		if ((state.sensorInHg == null) || (state.sensorInHg != pressure)) state.sensorInHg = pressure
		if (settings.referenceInHg != null) {
			state.InHgOffset = roundIt((settings.referenceInHg - pressure), 2)
			if (debugOn) log.debug "sensorInHg: ${pressure}, referenceInHg: ${referenceInHg}, offset: ${state.InHgOffset}"
			settings.referenceInHg = null
			device.updateSetting('referenceInHg', "")
			if (isHE) device.clearSetting('referenceInHg')
		}
		def offset = state.InHgOffset
		if (offset == null) {
			def InHg = device.currentValue('InHgOffset')	// convert the old attribute to the new state variable
			offset = (InHg != null) ? InHg : 0.0
			state.InHgOffset = offset
		}
    	if (offset != 0.0) {
    		def v = pressure
    		pressure = roundIt((v + offset), 2)
    	}
	}
    if (temperature != null) {
        sendEvent(name: 'temperature', value: temperature, unit: 'F')
        sendEvent(name: 'temperatureDisplay', value: roundIt(temperature, 0), unit: 'F', displayed: false)
    }
    if (humidity != null) {
        sendEvent(name: 'humidity', value: humidity, unit: '%')
    }
    if (pressure != null) {
        sendEvent(name: 'pressure', value: pressure, unit: 'inHg', displayed: false)
        sendEvent(name: 'pressureDisplay', value: pressure+'\ninHg', unit: '', descriptionText: "Barometric Pressure is ${pressure}inHg" )
    }
    
    def now = new Date(newest).format("h:mm:ss a '\non' M/d/yyyy", location.timeZone).toLowerCase()
    def locLabel = response.results[0]?.Label
    if (response.results[0]?.DEVICE_LOCATIONTYPE != 'inside') {
    	if (single < 2) {
    		locLabel = locLabel + '\nBad data from ' + ((single<0)?'BOTH channels':((single==0)?'Channel B':'Channel A'))
    	}
    } else {
    	if (single < 0) {
        	locLabel = locLabel + '\nBad data from ONLY channel (A)'
        }
    }
    sendEvent(name: 'locationName', value: locLabel)
    sendEvent(name: 'rssi', value: rssi, unit: 'db', descriptionText: "WiFi RSSI is ${rssi}db")
    sendEvent(name: 'ID', value: response.results[0]?.ID, descriptionText: "Purple Air Station ID is ${response.results[0]?.ID}")
    sendEvent(name: 'updated', value: now, displayed: false)
    sendEvent(name: 'timestamp', value: newest.toString(), displayed: false)	// Send last
}

private def pm_to_aqi(pm) {
	def aqi
	if (pm > 500) {
	  aqi = 500;
	} else if (pm > 350.5) {
	  aqi = remap(pm, 350.5, 500.5, 400, 500);
	} else if (pm > 250.5) {
	  aqi = remap(pm, 250.5, 350.5, 300, 400);
	} else if (pm > 150.5) {
	  aqi = remap(pm, 150.5, 250.5, 200, 300);
	} else if (pm > 55.5) {
	  aqi = remap(pm, 55.5, 150.5, 150, 200);
	} else if (pm > 35.5) {
	  aqi = remap(pm, 35.5, 55.5, 100, 150);
	} else if (pm > 12) {
	  aqi = remap(pm, 12, 35.5, 50, 100);
	} else if (pm > 0) {
	  aqi = remap(pm, 0, 12, 0, 50);
	} else { aqi = 0 }
	return aqi;
}

private def pm_to_caqi(pm) {
	def caqi					// based off of (hourly) pm2.5 only
	if (pm > 110) {
	  caqi = 100;
	} else if (pm > 55) {
	  caqi = remap(pm, 55, 110, 75, 100);
	} else if (pm > 30) {
	  caqi = remap(pm, 30, 55, 50, 75);
	} else if (pm > 15) {
	  caqi = remap(pm, 15, 30, 25, 50);
    } else caqi = remap(pm, 0, 15, 0, 25);
	return caqi;
}

private def remap(value, fromLow, fromHigh, toLow, toHigh) {
    def fromRange = fromHigh - fromLow;
    def toRange = toHigh - toLow;
    def scaleFactor = toRange / fromRange;

    // Re-zero the value within the from range
    def tmpValue = value - fromLow;
    // Rescale the value to the to range
    tmpValue *= scaleFactor;
    // Re-zero back to the to range
    return tmpValue + toLow;
}
private def logDebug(msg) { if (debugOn) log.debug(msg) }

def debugOff(){
    log.warn "debug logging disabled..."
    device.updateSetting("debugOn",[value:"false",type:"bool"])
	settings.debugOn = false
}
private roundIt( value, decimals=0 ) {
	return (value == null) ? null : value.toBigDecimal().setScale(decimals, BigDecimal.ROUND_HALF_UP) 
}
private roundIt( BigDecimal value, decimals=0) {
    return (value == null) ? null : value.setScale(decimals, BigDecimal.ROUND_HALF_UP) 
}
private def getAqiColors() {
	[	// Gradients don't work well - best to keep colors solid for each range
    	[value:   0, color: '#44b621'],		// Green - Good
        [value:  50, color: '#44b621'],
        [value:  51, color: '#f1d801'],		// Yellow - Moderate
        [value: 100, color: '#f1d801'],
        [value: 101, color: '#d04e00'],		// Orange - Unhealthy for Sensitive groups
        [value: 150, color: '#d04e00'],
        [value: 151, color: '#bc2323'],		// Red - Unhealthy
        [value: 200, color: '#bc2323'],
        [value: 201, color: '#800080'],		// Purple - Very Unhealthy
        [value: 300, color: '#800080'],
        [value: 301, color: '#800000']		// Maroon - Hazardous
    ]
}
private def getCaqiColors() {
	// Common Air Quality Index
	[
    	[value:   0, color: '#79bc6a'],		// Green - Very Low
        [value:  24, color: '#79bc6a'],
        [value:  25, color: '#bbcf4c'],		// Chartruese - Low
        [value:  49, color: '#bbcf4c'],
        [value:  50, color: '#eec20b'],		// Yellow - Medium
        [value:  74, color: '#eec20b'],
        [value:  75, color: '#f29305'],		// Orange - High
        [value:  99, color: '#f29305'],
        [value: 100, color: '#e8416f']		// Red - Very High
    ]
}
