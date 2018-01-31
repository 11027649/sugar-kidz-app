# Report

This is an application for dutch children with diabetes. The appropriate age range is 4-12 years. The application motivates the children to measure their bloodglucose and enter new measurements in the app. For each measurement they gain XP. With this XP they can buy pokemons. The pokemons can be seen in their own garden.

The application can also be used by parents. They can couple their account to their kid's account and then see their measurements. They will receive a notification if their kid enters a new measurement.

<img src="docs/homescreen.jpeg" width="30%" height="30%"/>    <img src="docs/homescreen_with_menu.jpeg" width="30%" height="30%"/>

## Technical design
Make a new design jpeg.

### Register/Log in Activity
You register or login as a user or as a parent.
If you're a parent you'll be redirected to the logbook activity.
If you're a "normal" user, you go to the main activity.

### Main Activity
Add new measurements, gain XP. (Update database.) See old measurements.
Go to the other Activities by using the menu.

### Pokeshop Activity
Only kids can enter this activity. Here the kids can spend their XP. The pokemons they own will be added to their Database. 

The pokemon are loaded from the api and saved into firebase. The API I wanted to use (and did use, but only just once) is: http://pokeapi.co/api/v2/pokemon/1/, for example. The 1 is a variable, 1 stands for pokemon one, that is, Bulbasaur.


### MyGarden Activity
Only kids can enter this activity. In the Garden they can see their pokemons living happily and change them to another pokemon they own by clicking on them.

### Logbook Activity
If you're a parent and not coupled yet, you'll see an empty logbook and the option "couple your phone". Once coupled, you'll see the logbook of your kid and receive notifications as they enter new measurements.

The kids can see their own, all-time, logbook.

### Couple Activity
In this activity the kids can see their username and create a couple code. This code is saved in FireBase for 10 minutes. 

The parent has to search their kid by their username and then couple to them by typing in the couple code they get from their kid.

### Databases, structure
Firebase Authentication Database as Firebase creates a FirebaseAuth.

My Firebase Database has the following structure:
1. pokemons
    1. pokemon number
        1. name
        1. sprite
1. users
    1. UID
        1. username
        1. amount of XP owned
        1. pokemon
            1. owned pokemon
                1. ints of pokemons owned
                1. displayed pokemon
                      1. ints of pokemons displayed
        1. measurements
            1. date
                1. time
                    1. title of this measurement (VO,NO,VL,NL,VA,NA,VS, extra (these are dutch moments of the day that are common                              moments to measure your blood glucose))
                    1. bloodglucose in mmol/L
### Classes
The classes that are used in this application are to simply add and receive information to firebase: we have User, SimpleMeasurement, Pokemon and Measurement.

There is also a class CodeGenerator, which is used to create the couple code parents need to link their account to their kids account.

## Challenges
SerialRequestQueue
Listening for Internet Connection
We had to do a lot of different things
How to save Bitmaps to Firebase

## Choiches to do things differently
The swipes
No snacks
If you're at a "good" height gain more XP
Background service for notifcations

## Final thoughts
I'm pretty happy about how my app turned out. It looks nice and does what I wanted it to do. There are some more things that I wanted to add in the beginning, but it is just to much for in these 4 weeks.

