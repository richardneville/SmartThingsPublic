definition (
        name: "Leave home secure",
        namespace: "richardneville",
        author: "Richard Neville",
        description: "Tells us if the doors and windows are open when we go out.",
        category: "My Apps",
        iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
        iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
        iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
        )

preferences {
    section("Set up things") {
        input "contactSensorThings", "capability.contactSensor", required: true, multiple: true, 
            title: "Select which things to watch"
        input "presenceThings", "capability.presenceSensor", required: true, multiple: true,
            title: "Notify when these things leave home"
        input "contactSensorTrigger", "capability.contactSensor", required: false,
            title: "Trigger when this thing closes (optional)"
    }
    section("Settings") {
        input "fullReport", "bool", title: "Provide full report?", required: true
        input "pushNotification", "bool", title: "Push the message out?", required: true
        input("recipients", "contact", title: "Send messages to") {
            input "phone", "phone", title: "Send an SMS?", description: "Phone Number", required: false
        }
        paragraph "If no recipients are selected, app will send a push notification."
    }
    
    section("Debug mode") {
        input "debugMode", "bool", title: "Use debug mode?", required: true
        //input "presenceThing", "capability.presenceSensor", required: true, title: "Which presenceSensor?"
        //input "motionSensorThing", "capability.motionSensor", required: true, title: "Which motionSensor?"
        input "switchThing", "capability.switch", required: false, title: "Which switch?"
    }
}

def installed() {
    initialize()
}

def updated() {
	unsubscribe()
    initialize()
}

def initialize() {
    subscribe(presenceThings, "presence", presenceHandler)
    
    subscribe(contactSensorTrigger, "contact", contactHandler)
    subscribe(switchThing, "switch", switchHandler)
    
    /*
    subscribe(presenceSensorThing, "presence", presenceHandler)
    subscribe(motionSensorThing, "motion", motionHandler)
    */
}

def presenceHandler(evt) {
    if (evt.value == "not present") {
        if (allThingsLeftHome()) {
            checkIfHouseSecure()
	    }
    }
}

def contactHandler(evt) {
	//if (contactSensorTrigger) {
        checkIfHouseSecure()
    //}
}

def switchHandler(evt) {
	if (debugMode) {
        checkIfHouseSecure()
    }
}

private checkIfHouseSecure() {
    for (contactSensorThing in contactSensorThings) {
        def sensorState = contactSensorThing.currentState("contact").value
        if (sensorState == "open" || fullReport) {
            sendMsg("${contactSensorThing.displayName} is ${sensorState} r1")
        }
    }
}

private allThingsLeftHome() {
    def result = true
    for (presenceThing in presenceThings) {
        if (presenceThing.currentPresence == "present") {
            // someone is present, so set result to false and terminate the loop.
            result = false
            break
        }
    }
    return result
}

private sendMsg(msg) {
    if (pushNotification) {
        // check that contact book is enabled and recipients selected
        if (location.contactBookEnabled && recipients) {
            sendNotificationToContacts(msg, recipients)
        } else if (phone) { // check that the user did select a phone number
            sendNotificationEvent("SMS sent to ${phone}")
            sendSms(phone, msg)
            //sendNotification(msg, [method: "phone", phone: "1234567890"])
        } else {
            sendPush(msg)
        }
    } else {
        sendNotificationEvent(msg)
    }
}

/*

def switchHandler(evt) {
    if (evt.value == "on") {
        //sendPush("The ${switchThing.displayName} is on!")
    } else if (evt.value == "off") {
        //sendPush("The ${switchThing.displayName} is off!")
    }
}

def motionHandler(evt) {
    if (evt.value == "active") {
    	// motion detected
        //sendPush("The ${motionSensorThing.displayName} detects activity!")
        //def msg = "${presenceSensorThing.displayName} is ${presenceSensorThing.currentState("presence").value}"
    } else if (evt.value == "inactive") {
        // motion stopped
        //sendPush("The ${motionSensorThing.displayName} detects nothing doing!")
    }
}

def contactHandler(evt) {
    if (evt.value == "open") {
        // contactSensor open
		sendPush("The ${contactSensorThing.displayName} is open!")
    } else if (evt.value == "closed") {
        // contactSensor closed
		sendPush("The ${contactSensorThing.displayName} is closed!")
    }
}

*/