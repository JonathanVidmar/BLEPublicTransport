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

_Prerequisites: The user has launched the application. The ticket price is independant of the destination._

1. The user arrives to the station and the application detects a beacon.
2. An in-app notification shows up displaying information about the ticket together with a buy option.
3. The users clicks buy and is taken to the ticket screen.

### Use case 5

_Prerequisites: The user has launched the application. The ticket price depends on the destination._

1. The user arrives to the station and the application detects a beacon.
2. An in-app notification shows up displaying information about the ticket together with a input field.
3. The users chooses the destination from a list and the price for the trip is displayed together with a buy option.
4. The users clicks buy and is taken to the ticket screen.

### Use case 6

_Prerequisites: The user has NOT launched the application._

1. The user arrives to the station and the application detects a beacon.
2. A notification is displayed telling the user to buy a ticket.
3. The user clicks the notification and then follows user case 3 or 4.

### Use case 7

_Prerequisites: The user has bought a ticket and app is running in the background._

1. The user arrives to the station and the application detects a beacon.
2. A notification is displayed telling the user that they have an active ticket.
3. The user clicks the notification and is taken to the ticket in the application.

## Iteration Three

## Iteration Four

## Iteration Five

## Iteration Six

## Final Iteration (wiieeiieiieei)
