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

*
*/
include 'asynchttp_v1'
import groovy.json.JsonSlurper

def getVersionNum() { return "1.0.00" }
private def getVersionLabel() { return "PurpleAir Air Quality Station, version ${getVersionNum()}" }

metadata {
    definition (name: "PurpleAir Air Quality Station", namespace: "sandood", author: "sandood") {
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Sensor"
        capability "Refresh"

        attribute "locationName", "string"
        attribute "pressure", "string"
        attribute "airQualityIndex", "string"
        attribute "aqi", "string"				// current AQI
		attribute "aqi10", "string"				// 10 minute average
		attribute "aqi30", "string"				// 30 minute average
		attribute "aqi1", "string"				// 1 hour average
		attribute "aqi6", "string"				// 6 hour average
		attribute "aqi24", "string"				// 24 hour average
		attribute "aqi7", "string"				// 7 day average
		attribute "pm", "string"				// current 2.5 PM (particulate matter)
		attribute "pm10", "string"				// 10 minute average
		attribute "pm30", "string"				// 30 minute average
		attribute "pm1", "string"				// 1 hour average
		attribute "pm6", "string"				// 6 hour average
		attribute "pm24", "string"				// 24 hour average
		attribute "pm7", "string"				// 7 day average
		attribute "rssi", "string"
        attribute 'message', 'string'
  		attribute "updated", "string"
        attribute "timestamp", "string"
        
        command "refresh"
    }

    preferences {
		input(name: "purpleID", type: "text", title: "${getVersionLabel()}\n\nPurpleAir Station ID", required: true, displayDuringSetup: true, description: 'Specify desired PurpleAir Station ID')
    	input(name: 'updateMins', type: 'enum', description: "Select the update frequency", 
        	title: "Update frequency (minutes)", displayDuringSetup: true, defaultValue: '5', options: ['1','5','10','15','30'], required: true)
    }
    
    tiles(scale: 2) {
        multiAttributeTile(name:"airQualityIndex", type:"generic", width:6, height:4, canChangeIcon: false) {
            tileAttribute("device.airQualityIndex", key: "PRIMARY_CONTROL") {
                attributeState("default", label:'${currentValue}', /*action: 'noOp',*/ defaultValue: true, 
					backgroundColors: [
						[value:   0, color: '#44b621'],		// Green - Good
						[value:  51, color: '#f1d801'],		// Yellow - Moderate
						[value: 101, color: '#d04e00'],		// Orange - Unhealthy for Sensitive groups
						[value: 151, color: '#bc2323'],		// Red - Unhealthy
						[value: 201, color: '#800080'],		// Purple - Very Unhealthy
						[value: 301, color: '#800000']		// Maroon - Hazardous
					]
				)
			}
            tileAttribute("device.message", key: "SECONDARY_CONTROL" ) {
				attributeState('default', label: '${currentValue}', defaultValue: true, icon: "https://raw.githubusercontent.com/SANdood/PurpleAirStation/master/images/purpleair.png")
			}
        }   
		valueTile('aqi', 'device.aqi', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: 'AQI\n${currentValue}',
            	backgroundColors: [
                	[value:   0, color: '#44b621'],		// Green - Good
                    [value:  51, color: '#f1d801'],		// Yellow - Moderate
                    [value: 101, color: '#d04e00'],		// Orange - Unhealthy for Sensitive groups
                    [value: 151, color: '#bc2323'],		// Red - Unhealthy
                    [value: 201, color: '#800080'],		// Purple - Very Unhealthy
                    [value: 301, color: '#800000']		// Maroon - Hazardous
                ]
        }
		valueTile('aqi10', 'device.aqi10', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: 'AQI\n${currentValue}',
            	backgroundColors: [
                	[value:   0, color: '#44b621'],		// Green - Good
                    [value:  51, color: '#f1d801'],		// Yellow - Moderate
                    [value: 101, color: '#d04e00'],		// Orange - Unhealthy for Sensitive groups
                    [value: 151, color: '#bc2323'],		// Red - Unhealthy
                    [value: 201, color: '#800080'],		// Purple - Very Unhealthy
                    [value: 301, color: '#800000']		// Maroon - Hazardous

                ]
        }
		valueTile('aqi30', 'device.aqi30', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: 'AQI\n${currentValue}',
            	backgroundColors: [
                	[value:   0, color: '#44b621'],		// Green - Good
                    [value:  51, color: '#f1d801'],		// Yellow - Moderate
                    [value: 101, color: '#d04e00'],		// Orange - Unhealthy for Sensitive groups
                    [value: 151, color: '#bc2323'],		// Red - Unhealthy
                    [value: 201, color: '#800080'],		// Purple - Very Unhealthy
                    [value: 301, color: '#800000']		// Maroon - Hazardous
                ]
        }
		valueTile('aqi1', 'device.aqi1', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: 'AQI\n${currentValue}',
            	backgroundColors: [
                	[value:   0, color: '#44b621'],		// Green - Good
                    [value:  51, color: '#f1d801'],		// Yellow - Moderate
                    [value: 101, color: '#d04e00'],		// Orange - Unhealthy for Sensitive groups
                    [value: 151, color: '#bc2323'],		// Red - Unhealthy
                    [value: 201, color: '#800080'],		// Purple - Very Unhealthy
                    [value: 301, color: '#800000']		// Maroon - Hazardous
                ]
        }
		valueTile('aqi6', 'device.aqi6', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: 'AQI\n${currentValue}',
            	backgroundColors: [
                	[value:   0, color: '#44b621'],		// Green - Good
                    [value:  51, color: '#f1d801'],		// Yellow - Moderate
                    [value: 101, color: '#d04e00'],		// Orange - Unhealthy for Sensitive groups
                    [value: 151, color: '#bc2323'],		// Red - Unhealthy
                    [value: 201, color: '#800080'],		// Purple - Very Unhealthy
                    [value: 301, color: '#800000']		// Maroon - Hazardous
                ]
        }
		valueTile('aqi24', 'device.aqi24', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: 'AQI\n${currentValue}',
            	backgroundColors: [
                	[value:   0, color: '#44b621'],		// Green - Good
                    [value:  51, color: '#f1d801'],		// Yellow - Moderate
                    [value: 101, color: '#d04e00'],		// Orange - Unhealthy for Sensitive groups
                    [value: 151, color: '#bc2323'],		// Red - Unhealthy
                    [value: 201, color: '#800080'],		// Purple - Very Unhealthy
                    [value: 301, color: '#800000']		// Maroon - Hazardous
                ]
        }
		valueTile('aqi7', 'device.aqi7', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: 'AQI\n${currentValue}',
            	backgroundColors: [
                	[value:   0, color: '#44b621'],		// Green - Good
                    [value:  51, color: '#f1d801'],		// Yellow - Moderate
                    [value: 101, color: '#d04e00'],		// Orange - Unhealthy for Sensitive groups
                    [value: 151, color: '#bc2323'],		// Red - Unhealthy
                    [value: 201, color: '#800080'],		// Purple - Very Unhealthy
                    [value: 301, color: '#800000']		// Maroon - Hazardous
                ]
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
            state "default", label: '${currentValue}'
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

        main(["aqi"])
        details([	"airQualityIndex",
					'aqi10', 'aqi30', 'aqi1', 'aqi6', 'aqi24', 'aqi7',
					'pm10', 'pm30', 'pm1', 'pm6', 'pm24', 'pm7',
					'updated', 'locationTile', 
					'temperature', 'humidity', 'pressure', 'rssi', 'ID', 'refresh',
				])
	}
}

def noOp() {}

// parse events into attributes
def parse(String description) {
    log.debug "Parsing '${description}'"
}

def installed() {
	initialize()
}

def uninstalled() {
	unschedule()
}

def updated() {
	log.info "Updated, settings: ${settings}"
    state.purpleAirStation = getVersionLabel()
	unschedule()
    initialize()
}

def initialize() {
	log.info 'Initializing...'
	if (purpleID) {
		// Schedule the updates
		def t = updateMins ?: '5'
		if (t == '1') {
			runEvery1Minute(getPurpleAirAQI)
		} else {
			"runEvery${t}Minutes"(getPurpleAirAQI)
		}
	}
	getPurpleAirAQI()
}

// handle commands
def poll() { refresh() }
def refresh() { getPurpleAirAQI() }
def configure() { updated() }

void getPurpleAirAQI() {
    if (!settings.purpleID) {
    	sendEvent(name: 'airQualityIndex', value: null, displayed: false)
        sendEvent(name: 'aqi', value: null, displayed: false)
        return
    }
    def params = [
        uri: 'https://www.purpleair.com',
        path: '/json',
        query: [show: settings.purpleID]
        // body: ''
    ]
    asynchttp_v1.get(purpleAirResponse, params)
}

def purpleAirResponse(response, data) {
	try {
        //log.debug "Purple Air json response is: $response.json"
        //send(name: 'purpleAir', value: response.json, displayed: false)
        state.purpleAir = response.json
    } catch (e) {
        log.error("Exception during Purple Air response processing", e)
		throw e
    }
	if (response.json) parsePurpleAir(response.json)
}

def parsePurpleAir(response) {
    def stats = [:]
    if (response.results[0]?.Stats) { stats[0] = new JsonSlurper().parseText(response.results[0].Stats) }
	if (response.results[1]?.Stats) { stats[1] = new JsonSlurper().parseText(response.results[1].Stats) }
		
	// check age of the data
    Long newest = ((stats[0]?.lastModified?.toLong() > stats[1]?.lastModified?.toLong()) ? stats[0].lastModified.toLong() : stats[1].lastModified.toLong())
    // log.debug "Timestamp: ${newest}"
    if (newest.toString() == device.currentValue('timestamp')) { log.info "No updates..."; return; } // nothing has changed yet
    
    Long age = now() - newest
    String oldData = ''
    // log.debug "Age: ${Math.round((age/1000).toFloat())} seconds"
    if	    (age > 604800000)	oldData = '1 week'
    else if (age > 172800000)	oldData = '2 days'
    else if (age > 86400000)	oldData = '1 day'
    else if (age > 43200000) 	oldData = '12 hours'
    else if (age > 3600000) 	oldData = '1 hour'
    else if (age > 300000)		oldData = '5 minutes'
    if (oldData != '') oldData = 'WARNING: No updates for more than ' + oldData
    
    // log.debug stats
    def single = null
	if (response.results[0].A_H) {
        if (response.results[1].A_H) {
        	// A bad, B bad
            single = -1
        } else {
        	// A bad, B good
        	single = 1
        }
    } else {
    	// Channel A is good
    	if (response.results[1].A_H) {
        	// A good, B bad
        	single = 0
        } else {
        	// A good, B good
            single = 2
        }
    }
	//log.info "Single: ${single}"
    Float pm
    Float pm10
    Float pm30
    Float pm1
    Float pm6
    Float pm24
    Float pm7
    def rssi
    if (single >= 0) {
    	if (single == 2) {
            pm   = (stats[0].v.toFloat()  + stats[1].v.toFloat()  ) / 2.0
            pm10 = (stats[0].v1.toFloat() + stats[1].v1.toFloat() ) / 2.0
            pm30 = (stats[0].v2.toFloat() + stats[1].v2.toFloat() ) / 2.0
            pm1  = (stats[0].v3.toFloat() + stats[1].v3.toFloat() ) / 2.0
            pm6  = (stats[0].v4.toFloat() + stats[1].v4.toFloat() ) / 2.0
            pm24 = (stats[0].v5.toFloat() + stats[1].v5.toFloat() ) / 2.0
            pm7  = (stats[0].v6.toFloat() + stats[1].v6.toFloat() ) / 2.0
 			rssi = Math.round((response.results[0].RSSI.toFloat() + response.results[1].RSSI.toFloat()) / 2.0)
        } else {
            pm   = stats[single].v.toFloat()
            pm10 = stats[single].v1.toFloat()
            pm30 = stats[single].v2.toFloat()
            pm1  = stats[single].v3.toFloat()
            pm6  = stats[single].v4.toFloat()
            pm24 = stats[single].v5.toFloat()
            pm7  = stats[single].v6.toFloat()
            rssi = response.results[single].RSSI.toInteger()
        }
    } else {
    	// No valid data...now what?
        oldData = "ERROR: Station ${purpleID} has no trusted sensor reports"
    }
    //log.info "RSSI: ${rssi}"
    
    if (single >= 0) {
        String aqi   = decString(pm_to_aqi(pm),1)
        String aqi10 = intString(pm_to_aqi(pm10))
        String aqi30 = intString(pm_to_aqi(pm30))
        String aqi1  = intString(pm_to_aqi(pm1))
        String aqi6  = intString(pm_to_aqi(pm6))
        String aqi24 = intString(pm_to_aqi(pm24))
        String aqi7  = intString(pm_to_aqi(pm7))

        sendEvent(name: 'airQualityIndex', 	value: aqi) // intString(pm_to_aqi(pm)))
        String p25 = decString(pm,1) + ' µg/m³'
        if (oldData == '') {
            if 		(aqi.toFloat() < 51)  sendEvent(name: 'message', value: ' GOOD - little to no health risk '+ "(${p25})")
            else if (aqi.toFloat() < 101) sendEvent(name: 'message', value: ' MODERATE - slight risk for some people '+ "(${p25})")
            else if (aqi.toFloat() < 151) sendEvent(name: 'message', value: ' UNHEALTHY for Sensitive Groups '+ "(${p25})")
            else if (aqi.toFloat() < 201) sendEvent(name: 'message', value: ' UNHEALTHY for most people '+ "(${p25})")
            else if (aqi.toFloat() < 301) sendEvent(name: 'message', value: ' VERY UNHEALTHY - serious effects for everyone '+ "(${p25})")
            else 						  sendEvent(name: 'message', value: ' HAZARDOUS - emergency conditions for everyone '+ "(${p25})")
        } else {
            sendEvent(name: 'message', value: oldData, descriptionText = "No updates for ${Math.round((age/60000).toFloat())} minutes")
            log.error "No updates for ${Math.round((age/60000).toFloat())} minutes"
        }
		log.info "AQI: ${aqi}"
        
        sendEvent(name: 'aqi', 	 value: aqi)
        sendEvent(name: 'aqi10', value: aqi10)
        sendEvent(name: 'aqi30', value: aqi30)
        sendEvent(name: 'aqi1',  value: aqi1)
        sendEvent(name: 'aqi6',  value: aqi6)
        sendEvent(name: 'aqi24', value: aqi24)
        sendEvent(name: 'aqi7',  value: aqi7)

        sendEvent(name: 'pm',   value: pm,   unit: 'µg/m³')
        sendEvent(name: 'pm10', value: pm10, unit: 'µg/m³')
        sendEvent(name: 'pm30', value: pm30, unit: 'µg/m³')
        sendEvent(name: 'pm1',  value: pm1,  unit: 'µg/m³')
        sendEvent(name: 'pm6',  value: pm6,  unit: 'µg/m³')
        sendEvent(name: 'pm24', value: pm24, unit: 'µg/m³')
        sendEvent(name: 'pm7',  value: pm7,  unit: 'µg/m³')
    } else {
    	sendEvent(name: 'message', value: oldData) // ERROR
    }

    sendEvent(name: 'locationName', value: response.results[0].Label)
    String temperature = decString((response.results[0].temp_f.toFloat() + response.results[1].temp_f.toFloat()) / 2.0, 1)
    sendEvent(name: 'temperature', value: temperature, unit: 'F')
    sendEvent(name: 'temperatureDisplay', value: Math.round(temperature.toFloat()), unit: 'F', displayed: false)
    String humidity = intString((response.results[0].humidity.toFloat() + response.results[1].humidity.toFloat()) / 2.0)
    sendEvent(name: 'humidity', value: humidity, unit: '%')
    String pressure = decString((((response.results[0].pressure.toFloat() + response.results[1].pressure.toFloat()) / 2.0) * 0.02953), 2)
    sendEvent(name: 'pressure', value: pressure, unit: 'inHg', displayed: false)
    sendEvent(name: 'pressureDisplay', value: pressure+'\ninHg', unit: '', descriptionText: "Barometric Pressure is ${pressure}inHg" )
    
    //def newest = ((stats[0].lastModified?.toLong() > stats[1].lastModified?.toLong()) ? stats[0].lastModified.toLong() : stats[1].lastModified.toLong())
    def now = new Date(newest).format('HH:mm:ss MM/dd/yyyy', location.timeZone)
    if (single < 2) {
    	now = now + '\nBad data from ' + ((single<0)?'BOTH channels':((single==0)?'Channel B':'Channel A'))
    }
    sendEvent(name: 'updated', value: now, displayed: false)
    sendEvent(name: 'timestamp', value: newest.toString(), displayed: false)
    sendEvent(name: 'rssi', value: rssi, unit: 'db', descriptionText: "WiFi RSSI is ${rssi}db")
    sendEvent(name: 'ID', value: response.results[0].ID, descriptionText: "Purple Air Station ID is ${response.results[0].ID}")
}

private Float pm_to_aqi(pm) {
	def aqi
	if (pm > 500) {
	  aqi = 500;
	} else if (pm > 350.5 && pm <= 500 ) {
	  aqi = remap(pm, 350.5, 500.5, 400, 500);
	} else if (pm > 250.5 && pm <= 350.5 ) {
	  aqi = remap(pm, 250.5, 350.5, 300, 400);
	} else if (pm > 150.5 && pm <= 250.5 ) {
	  aqi = remap(pm, 150.5, 250.5, 200, 300);
	} else if (pm > 55.5 && pm <= 150.5 ) {
	  aqi = remap(pm, 55.5, 150.5, 150, 200);
	} else if (pm > 35.5 && pm <= 55.5 ) {
	  aqi = remap(pm, 35.5, 55.5, 100, 150);
	} else if (pm > 12 && pm <= 35.5 ) {
	  aqi = remap(pm, 12, 35.5, 50, 100);
	} else if (pm > 0 && pm <= 12 ) {
	  aqi = remap(pm, 0, 12, 0, 50);
	}
	return aqi;
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

private String decString( value, decimals ) {
	return (value == null) ? '--' : String.format("%.${decimals}f", value.toFloat().round(decimals))
}
private String intString( value )  {
	return (value == null) ? '--' : Math.round(value.toFloat())
}
