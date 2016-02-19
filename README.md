# Master Thesis Android Application
This github contains the application which was developed for our thesis work. It is a prototype of how the Bluetooth Smart technology can be used in a public transport system.

## Week log
The weeks are numbered as they are i the normal year, so week one would be the first week of the year 2016.
The logs begin with a general description on what has occurred during the week, and ends with a smaller note on what each person has done beside the cooperative work.

### Week five 
Last week our project plan was finished and this week started by conducting interviews with a certain number of people. The interviews only showed what we already suspected but will be a good method to show it in the final report. The beginning of the week also included continuing our research into the different technologies we will be using.

In the middle of the week we started setting up the environments we will be using to develop the prototypes. We installed Android Studios/IntelliJ and set up our project with github. In the end of the week we started developing for our use cases, finishing both the first and the second.
We also set up the initial structure of the report and inserted relevant parts of the project plan into it.

**Jonathan**
Implemented use case 1 consisting both Android implementation as well as a web page used for redirection.

**Jacob**
Implemented use case 2 with focus on both foreground and background beacon detection and notification integration.

### Week six
This week we continued on our first iteration. We decided in the beginning to try Google's new Nearby and Proximity API. It was not very easy to implement this since it's new and doesn't have a lot of documentation. In the end the API didn't really do what we wanted and we decided to use the Android Beacon Library instead. This was probably a good choice but it was a shame that we used so much time on Google Nearby. 

The last two days of the week went into trying to get the last user case implemented and also to do some restructuring to make the application easier to work with in the future.

**Jonathan**
Implemented a lot of the server side authentication towards Google Nearby, and later tried to fix Nearby to work with our application. When implementing Android Beacon Library Jonathan did a lot of the restructuring of the application.

**Jacob**
Installed the beacons and set up the managing panel of them. Looked into Nearby and how to implement it. Implemented the list view with the nearby objects for Use Case 3.

### Week seven
This week we started with the second iteration. The second iteration included being able to buy a ticket in the application, propted by the beacon. In the beginning of the week a lot of work went into making the application more stable. We had a meeting with the supervisor at LTH on Thuesday. The end of the week the new use cases was completely implemeted and the last day of the week went into researching methods for how we are going to implement the functionalities for the next iteration. Iteration 2 was suppose to be finished next wednesday but since we are already done, the next iteration will begin on monday.
**Jonathan**
Jonathan worked a lot with implementing the notifications that brings the user into the application. He created a module to send different notifications depending on the settings screen on the phone. 
**Jacob**
Jacob implemented the settings screen, the payment screen and the ticket screen. These were relativly easy screens to implement since they only display information and have little logic behind them. He also started writing about the theoretical background in the thesis report.