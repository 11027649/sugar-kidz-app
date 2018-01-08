# Activities
For each activitie, different functions and databases are needed.

## Register/ Log in activity
Firebase Authentication Database.

1. users
    1. emailadress
    1. time of registration
    1. hash of password
    1. user uid
    
_When registered_
1. users
  1. UID
      1. username
      1. amount of XP owned
      1. bloedsugars
          1. date
              1. title of this measurement (VO,NO,VL,NL,VA,NA,VS, extra (these are dutch moments of the day that are common moments to measure your blood glucose))
              1. time
              1. bloedglucose in mmol/L
      1. owned pokémon
          1. name
              1. figure of the pokémon

## parent activity
Pop Up screen: connect to child's app by giving his username and a secret code

_The activitie itself_

Get an alert if your child enters a new measurement.
ListView of your child's bloodsugars.

Maybe: Send your child a reminder to check their bloodsugar.

## main activity
Add new measurements, gain XP. (Update database.) See old measurements. 
Maybe: Set an alarm for when to check your next bloodsugar.

## snack ideas (this is an optional activity)
This would need a new database. (This is possible in firebase.)
A database made and used by the users; you can add a snack and the amount of carbs.

## pokeshop activity
Buy pokemons from your XP. Get a pop-up to ask are you sure to buy this pokémon? If yes, distract XP and add Pokemon to your database. 

### my garden activity
See your pokemon living happy in a landscape. (Maybe later let the users buy different landscapes?)
