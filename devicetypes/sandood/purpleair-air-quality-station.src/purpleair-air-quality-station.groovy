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
*
*/
// If building on/for hubitat, comment out the next line
include 'asynchttp_v1'

import groovy.json.JsonSlurper
import java.math.BigDecimal

def getVersionNum() { return "1.0.13" }
private def getVersionLabel() { return "PurpleAir Air Quality Station, version ${getVersionNum()}" }

metadata {
    definition (name: "PurpleAir Air Quality Station", namespace: "sandood", author: "sandood") {
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Signal Strength"
        capability "Sensor"
        capability "Refresh"

        attribute "locationName", "string"
        attribute "ID", "string"
        attribute "pressure", "number"
        attribute "airQualityIndex", "string"
        attribute "aqi", "number"				// current AQI
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
		attribute "rssi", "number"				// Signal Strength attribute (not supporting lqi)
        attribute 'message', 'string'
  		attribute "updated", "string"
        attribute "timestamp", "string"
        
        command "refresh"
    }

    preferences {
		input(name: "purpleID", type: "text", title: "${getVersionLabel()}\n\nPurpleAir Station ID", required: true, displayDuringSetup: true, description: 'Specify desired PurpleAir Station ID')
    	input(name: 'updateMins', type: 'enum', description: "Select the update frequency", 
        	title: "Update frequency (minutes)", displayDuringSetup: true, defaultValue: '5', options: ['1','3','5','10','15','30'], required: true)
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
    // If building on/for hubitat, comment out the next line, and uncomment the one after it
    asynchttp_v1.get(purpleAirResponse, params)		// For SmartThings
    // asynchttpget(purpleAirResponse, params)		// For hubitat
}

def purpleAirResponse(resp, data) {
	if (resp && (resp.status == 200)) {
		try {
			if (resp.json) {
				//log.trace "Response: ${resp.json}"
                log.info "purpleAirResponse() got JSON..."
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
	if (!response.results[0]?.Stats && !response.results[1]?.Stats) {
    	log.error "Invalid API response: ${response}"
        return
    }
    //log.debug response
    log.info "Parsing PurpleAir ${response.results[0].DEVICE_LOCATIONTYPE} sensor report"
    
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
    if (newest?.toString() == device.currentValue('timestamp')) { log.info "No update..."; return; } // nothing has changed yet
    
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
	log.info "Single: ${single}"
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
 			if (response.results[0].RSSI?.isNumber() && response.results[1].RSSI?.isNumber()) rssi = roundIt(((response.results[0].RSSI.toBigDecimal() + response.results[1].RSSI.toBigDecimal()) / 2.0), 0)
        } else {
        	pm   = roundIt(stats[single].v, 2)
            pm10 = roundIt(stats[single].v1, 2)
            pm30 = roundIt(stats[single].v2, 2)
            pm1  = roundIt(stats[single].v3, 2)
            pm6  = roundIt(stats[single].v4, 2)
            pm24 = roundIt(stats[single].v5, 2)
            pm7  = roundIt(stats[single].v6, 2)
            rssi = roundIt(response.results[single].RSSI, 0)
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
            sendEvent(name: 'message', value: oldData, descriptionText = "No updates for ${roundIt((age/60000),2)} minutes")
            log.error "No updates for ${roundIt((age/60000),2)} minutes"
        }
		log.info "AQI: ${aqi}"
        
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
    if (response.results[0].temp_f?.isNumber() && response.results[1].temp_f?.isNumber()) 
    	temperature = roundIt(((response.results[0].temp_f.toBigDecimal() + response.results[1].temp_f.toBigDecimal()) / 2.0), 1)
    sendEvent(name: 'temperature', value: temperature, unit: 'F')
    sendEvent(name: 'temperatureDisplay', value: roundIt(temperature, 0), unit: 'F', displayed: false)
    def humidity
    if (response.results[0].humidity?.isNumber() && response.results[1].humidity?.isNumber()) 
    	humidity = roundIt(((response.results[0].humidity.toBigDecimal() + response.results[1].humidity.toBigDecimal()) / 2.0), 0)
    sendEvent(name: 'humidity', value: humidity, unit: '%')
    def pressure
    if (response.results[0].pressure?.isNumber() && response.results[1].pressure?.isNumber()) 
    	pressure = roundIt((((response.results[0].pressure.toBigDecimal() + response.results[1].pressure.toBigDecimal()) / 2.0) * 0.02953), 2)
    sendEvent(name: 'pressure', value: pressure, unit: 'inHg', displayed: false)
    sendEvent(name: 'pressureDisplay', value: pressure+'\ninHg', unit: '', descriptionText: "Barometric Pressure is ${pressure}inHg" )
    
    def now = new Date(newest).format("h:mm:ss a '\non' M/d/yyyy", location.timeZone).toLowerCase()
    def locLabel = response.results[0].Label
    if (response.results[0].DEVICE_LOCATIONTYPE != 'inside') {
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
    sendEvent(name: 'ID', value: response.results[0].ID, descriptionText: "Purple Air Station ID is ${response.results[0].ID}")
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
