# Master Thesis Android Application
This github contains the application which was developed for our thesis work. It is a prototype of how the Bluetooth Smart technology can be used in a public transport system.

## Week log
The weeks are numbered as they are i the normal year, so week one would be the first week of the year 2016.
The logs begin with a general description on what has occurred during the week, and ends with a smaller note on what each person has done beside the cooperative work.

### Week four 
Last week our project plan was finished and this week started by conducting interviews with a certain number of people. The interviews only showed what we already suspected but will be a good method to show it in the final report. The beginning of the week also included continuing our research into the different technologies we will be using.

In the middle of the week we started setting up the environments we will be using to develop the prototypes. We installed Android Studios/IntelliJ and set up our project with github. In the end of the week we started developing for our use cases, finishing both the first and the second.
We also set up the initial structure of the report and inserted relevant parts of the project plan into it.

**Jonathan**
Implemented use case 1 consisting both Android implementation as well as a web page used for redirection.

**Jacob**
Implemented use case 2 with focus on both foreground and background beacon detection and notification integration.

### Week five
This week we continued on our first iteration. We decided in the beginning to try Google's new Nearby and Proximity API. It was not very easy to implement this since it's new and doesn't have a lot of documentation. In the end the API didn't really do what we wanted and we decided to use the Android Beacon Library instead. This was probably a good choice but it was a shame that we used so much time on Google Nearby. 

The last two days of the week went into trying to get the last user case implemented and also to do some restructuring to make the application easier to work with in the future.

**Jonathan**
Implemented a lot of the server side authentication towards Google Nearby, and later tried to fix Nearby to work with our application. When implementing Android Beacon Library Jonathan did a lot of the restructuring of the application.

**Jacob**
Installed the beacons and set up the managing panel of them. Looked into Nearby and how to implement it. Implemented the list view with the nearby objects for Use Case 3.

### Week six
This week we started with the second iteration. The second iteration included being able to buy a ticket in the application, propted by the beacon. In the beginning of the week a lot of work went into making the application more stable. We had a meeting with the supervisor at LTH on Thuesday. The end of the week the new use cases was completely implemeted and the last day of the week went into researching methods for how we are going to implement the functionalities for the next iteration. Iteration 2 was suppose to be finished next wednesday but since we are already done, the next iteration will begin on monday.

**Jonathan**
Jonathan worked a lot with implementing the notifications that brings the user into the application. He created a module to send different notifications depending on the settings screen on the phone. 

**Jacob**
Jacob implemented the settings screen, the payment screen and the ticket screen. These were relativly easy screens to implement since they only display information and have little logic behind them. He also started writing about the theoretical background in the thesis report.

### Week seven and eight
This week started with iteration three, where the focus was to implement the automatic payment functioanlity. This would enable a user to walk pass a payment point and without activly using their phone validate or pay for their trip. We choose to this by creating another application which would run on a tablet, where a message will display if the payment/validation was successful when the user walks by. In reality this application could be replaced by physical gates, which can open and close, etc.

**Jonathan**
Jonathan worked on estimating distances to the beacon, to be able to know when the gates is going to open. The AltBeacon SDK has built in functionality for this but it wasn't accurate enough. Using the Log-distance path loss model the distance was estimated with the RSSI values received from the beacon. A Kalman filter was used to make the results more stable and not be affected by fluctuations in the signals. A walking detection algorithm was also added to provide more information and to get the estimates even more precise. In a last effort to improve the estimates, a self correcting beacon was added.

**Jacob** 
Jacob worked on the new application, called the Gate app. Creating the application and then making it as a Bluetooth server where nearby devices can connect to, through bluetooth pairing, to open the gates. This was more difficult than we first thought, and in the end it didn't really work as well as we hoped. A few other ways was investigated (using BLE all the way or by connecting to a webserver) which we will talk more about in the report. It was decided that we would stick with our bluetooth solution because it would take to much time to implement anything else and for our use cases it would not make any difference.

### Week nine
This week we prepared for our usability tests and performed them. We started by writing a usability test plan, and on thursday and friday we performed the tests on a few persons. Friday afternoon was spent analysing the results from the tests.

### Week ten
Skiing in the alpes.
[[https://github.com/JonathanVidmar/BLEPublicTransport/blob/master/important_image.jpg]]
![Skiing](important_image.jpg?raw=true "Skiing")
### Week eleven
This week started by going through the new iterration. We realised that one of the things we were going to do during this iterration was unneccesairy and decided together with our supervisor not to do it. Instead we will focus this iterration on improving the system related to the feedback we got from the usability tests.

**Jonathan**
Jonathan has been improving the Kalman filter used in the distance estimation since the tests showed our distance calculations was off. 

**Jacob**
Jacob has been working on improving the user interface related to the results of the usability tests.

### Week twelve
This week has been a lot of focus on the report. We have finished writing most of the background sections, apart from a few sections we will leave until the end. We have also written a lot on the result sections, since we now have results both in the form of a prototype and as usability test results. 

***Jonathan***
Jonathan has written about Kalman filters, walk detection and self correcting beacons.

***Jacob***
Jacob has focused more on the usability test, results, starting the discussion about them, and finishing the background.

###Week thirteen
This week has focused more on the context based part of the application. The nearby page has been remade and made smarter, to show information based on the location of the user.

***Jonathan***
Jonathan started looking on what information should be displayed when the application notices that the user is on a public transport vehicle, like a train. 

***Jacob*** 
Jacob has improved the page displaying information about the current station and also started working on a unified design of the entire application.
