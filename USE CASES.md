# Iteration schedule with use cases
_**General prerequisite**_

_For all use cases, the user is assumed to have a smartphone with support for BLE (Bluetooth Low Energy) capabilities and depending on use case, support for either Eddystone or iBeacon protocol._
_For use cases that involve payment methods, the user is assumed to have registered an account previously with a valid method of payment e.g credit card. For all other use cases, the user is assumed to have the application installed on their phone, unless specified otherwise._

## Iteration One

### Use case 1 (currently eddystone only)

_Prerequisites: The user has an iPhone with iOS 8 or higher and has activated the widget Chrome Today, but hasn’t installed any application. (Also works with Android if user has Chrome Dev and activated Beacon Proximity Flag)_

1. The user walks into the station and sees a notification from the chrome widget in the notification center on his phone.
2. The user presses the notification and is brought to the App Store via a web page redirect, prompted to install the application.

### Use case 2

_Prerequisites: None_

1. The user walks into the station and receives a notification about the application.
2. The user presses the notification and is brought to the main screen of the application.

### Use case 3

_Prerequisites: The user has launched the application._

1. The user arrives to the station and is taken to the main screen when a beacon is detected.
2. While moving around, the user can see the distances to any point of interest in a list.

## Iteration Two

### Use case 4

_Prerequisites: The user has NOT launched the application._

1. The user arrives to the station and the application detects a beacon.
2. A notification is displayed telling the user to buy a ticket.
3. The ticket price is independant of the destination:
  1. A user clicks the notification and a screen shows up displaying information about the ticket together with a buy option.
  2. The users clicks buy and is taken to the ticket screen.
4. The ticket price is dependant of the destination.
  1. A user clicks the notification and a screen shows up displaying information about the ticket together with a input field.
  2. The users chooses the destination from a list and the price for the trip is displayed together with a buy option.
  3. The users clicks buy and is taken to the ticket screen.


### Use case 5

_Prerequisites: The user has bought a ticket and app is running in the background._

1. The user arrives to the station and the application detects a beacon.
2. A notification is displayed telling the user that they have an active ticket.
3. The user clicks the notification and is taken to the ticket in the application.

## Iteration Three

### Use case 6

_Prerequisites: The user has bought a a subscription and app is running in the background. To enter the transportation vehicle the user has to pass physical gates (gates are represented by another android application in the prototype)._

1. The user is at the station and is about the go through the gates.
2. The phone recognizes the beacon and starts the validation
3. Validation is a success:
  1. The gates display a success message on a screen.
  2. The gates open.
4. Validation is a failure:
  1. The gates display fail message on a screen.
  2. The gates does not open.


### Use case 7

_Prerequisites: The user does NOT have a valid ticket and app is running in the background. To enter the transportation vehicle the user has to pass physical gates (gates are represented by another android application in the prototype)._

1. The user is at the station and is about the go through the gates.
2. The phone recognizes the beacon
3. The user buys a single ticket and the gate then performs validation
4. Validation is a success:
  1. The gates display a success message on a screen.
  2. The gates open.
5. Validation is a failure:
  1. The gates display fail message on a screen.
  2. The gates does not open.

### Use case 8

_Prerequisites: The user is at the station and has bought a ticket._

1. The user approaches the gates with the "connect to gate"-screen open.
2. When the user is within a certain distance a button to open the gates becomes visible.
3. The user clicks the button and validation is performed.
4. Validation is a success:
  1. The gates display a success message on a screen.
  2. The gates open.
5. Validation is a failure:
  1. The gates display fail message on a screen.
  2. The gates does not open.

## Iteration Four

### ~~Use case 9~~

_~~Prerequisites:~~_

1. ~~The user approaches a pay machine at the station.~~
2. ~~The user chooses to buy a digital ticket.~~
3. ~~The user chooses his own account displayed on the screen.~~
4. ~~The user proceeds with the purchase process as per previously implemented.~~
5. ~~The user pays with either cash or card.~~
6. ~~The user receives a notification in his/her phone of the newly bought ticket.~~

### Use case 10

_Prerequisites:_

1. The user is about to begin his journey and checks in at the current station, either by the option in the notification or in the application.
2. The user has finished his journey and leaves the transportation vehicle/station.
3. The user receives information about the finished journey, e.g cost, duration etc.

### Improvements to previous use cases as brought up by the first round of usability testing
* Use case 8 is modified to using a notification instead of a separate screen. When the notification is clicked the gates are opened if validation is successful.
* In use case 6-7 the user receives feedback in form of vibration when the gate is opened.
* Extend nearby list by adding funtionality to make navigation to object easier.
* Nearby list less detailed.
* Look into lock screen notifications.
* Add alert before performing buy action.


## Iteration Five

## Iteration Six

## Final Iteration (wiieeiieiieei)
