# PurpleAirStation
PurpleAir Air Quality Station for SmartThings

Provides continuously updated Air Quality Index based on the PM2.5 data supplied by a Purple Air air quality sensor (see www.purpleair.com for more information).
### Simple Setup
To use this Air Quality Station in SmartThings, you will need to:
1. Create a new SmartThings device handler using the supplied source code 
  - Edit the code in 2 places if using hubitat - search for "hubitat"
2. Create the actual device: log into the IDE, select your Location, select Devices and then select Create New Device
2. Specify your preference for update frequency
3. Specify the desired PurpleAir Station ID you want to monitor

To find a specific Station, 
A. Use the PurpleAir Map to locate a station (https://www.purpleair.com/map)
B. Open this URL in a new Window or Tab: (https://www.purpleair.com/json)
C. Search for the NAME of the station you found in step A
D. The Station ID is the first element in the results[:] map - you will enter this ID (1-5 digit number) into the preferences for the Air Quality Station

That's it - save, and you should be up and running!

### Change Log
*	1.0.00 - Initial Release
*	1.0.01 - Cleanup of description Texts & bug fixes
*	1.0.02 - Fixed some typos
*	1.0.03 - More string edits
*	1.0.04 - Updated icons & color handling
*	1.0.05 - Now uses BigDecimal for maximum precision
*	1.0.06 - Finalized conversion to BigDecimal
*	1.0.07 - Better error handling
*	1.0.08 - Changed all numberic attributes to "number"
*	1.0.09 - Changed to maintain and display only integer AQI (decimals are distracting)
*	1.0.10 - Fixed room/thing tile display
*	1.0.11 - Handles Inside PurpleAir Sensor (only 1 sensor by design)
*	1.0.12 - Internal cleanup of Inside sensor support, added runEvery3Minutes
*	1.0.13 - Code annotations for hubitat users

#### Donations
As always, my contributions to the SmartThings community are entirely free, but should you feel compelled to make a donation, you can do so here: https://paypal.me/BarryABurke

## Screen Shot:
<img src="https://raw.githubusercontent.com/SANdood/PurpleAirStation/master/images/PurpleAirStation.jpg" border="1" height="900" /> 
