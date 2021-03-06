# The process of making this application
How I have built towards the final version of this application can be read here. How I started from only an idea on day 1 and a fully working application in the end, with only slightly less functions than I imagined. 
NB: this file is only committed once, because I wasn’t aware we should keep this process book on GitHub. I kept it in a local Word-file on my computer.
## Week 1
This was the week I planned the real app, from my idea. This was done by making a project proposal and a Design file, in which we described how our app should work, the structure of our data and the features of our app. 

At Wednesday already, I tried to request Pokémon sprites from the API I would use. This worked, so my plans could go on! Before Friday, we had to make a prototype of the app, with all the Activities already made (they didn’t had to look pretty) and the navigation between these screens. I used swipe listeners to go from one activity to the other, and this was my prototype. 

It wasn’t much more than that, I didn’t do as much as I planned to do and loading 151 Pokémon only showed like, 4, or when Volley was feeling lazy, not even 1. I didn’t understand what was going on, but saved this for next weeks, because I got flu on Thursday. So, I only came for the first Stand-Up and on Friday I stayed home and didn’t do anything but lying in my bed.

## Week 2
On Monday (15-1-18) I linked my application to Firebase, so the user could register and then log in. I also built pretty lay-outs for these screens. The request for Pokémon has extended from only their sprite to also their names. 

On Thursday (16-1-18) I wanted to add measurements to Firebase, but this cost more time than I expected. I did make a TimePickerDialog and a DatePickerDialog to add dates and times to your measurement. I also made a spinner with label-choices. For these choices, see docs/attachment1_spinneroptions.md.

Wednesday (17-1-18) I finished the measurements, they are now added to Firebase. I also picked a background for the garden. The Pokémon still won’t load from the API. It only shows between 1 and 8 Pokémon.

Thursday (18-1-18) I made my Style Guide.  And worked on the parent environment.

Friday (19-1-18) I presented the Alpha version of my app. 

## Week 3
On Monday (22-1-18) I started making the parent environment. I finished my style guide and pushed it. All measurements are now showed in the logbook.

On Tuesday (23-1-18) I made a menu. The swipe navigation doesn’t have a pretty animation and doesn’t work on all items of the layout (for example, it doesn’t work on a List View). Therefore I decided to make a menu instead to navigate everywhere and to be able to log out.

On Wednesday (24-1-18) I solved the problem from loading the Pokémon, together with Renske. I didn’t know Volley hates being asked too much at a time. And I asked him 151 requests in a for loop. With a SerialRequestQueue, that is to be seen in docs/attachment2_serialrequestqueue.java, I requested all Pokémon. This loaded into the List View, but it took about 10 minutes. So, after all, I decided to load all the Pokémon to firebase and then load the List View from there.

On Thursday (25-1-18) today I fixed some small things for the functionality. In the garden the Pokémon that where there the last time you left are remembered and loaded the next time you enter. Buying Pokémon is still free, so that has to cost XP. I have made a small TODO list with little bugs.
1.	The on Click listener of the Pokémon image view stays at the old place when you add an animation to the Pokémon. So the Pokémon is at one side of the screen, but the on Click listener is still at the other side.
1.	Logbook headers aren’t right: when you scroll through the list the old headers are somehow recycled so the same headers are used over and over again.
1.	Sometimes in the garden the navigation bar doesn’t disappear. 
I’m going to do some of this over the weekend, and the rest of it next week.

On Friday (26-1-18) I presented my beta application. Marijn gave me the awesome tip to save my dates in another way to firebase, so it will still be ordered logically if we get to February.

## Week 4
This week was for debugging and making our application look pretty. In my application, there were some nasty bugs left and I removed them all during this week. 

On Monday and Tuesday (29-12-18 & 30-1-18) I was busy solving bugs. A list of these bugs: if you added a new measurement, the logbook added all the measurements again in the list view, so everything was double. The time was also saved weirdly (if you added a measurement at 01:05, it was saved as 1:5) and I fixed this using a Time- Standard Date Format. You could still buy Pokémon you already had, so I fixed this and added colors to the buttons with buyed/ not buyed. I made the navigation in the app prettier by using back buttons in the navigation bar instead of Text View saying “back”. I decided not to make a background service because I don’t have very much time left and the background services will die at Android 8. Also I started checking the internet connection to make sure the app doesn’t stop if you lose your connection.

On Wednesday (31-1-18), I solved my last two bugs: listening for internet connection and the logbook that was in the wrong order in the total logbook, with the last day all the way down the List View. I have tried several times to reverse the data source for this logbook in the past weeks, but because I’ve worked with headers in this List View, it wasn’t as easy as that. So, I let the List View automatically scroll all the way down to this last day (credits to Christian Bijvoets, for saving me. I was so stuck in my own ideas I didn’t even think of this). The internet connection wasn’t easy as well, I had to use an interface to close activities from the Main Activity (and I didn’t ever us one before). 

But after these two things worked, I was totally happy with my end product. I did some code refactoring, added some more comments, made my anonymous listeners not anonymous anymore, and started my report. 
Oh and before I forget: I also asked some people to break my app. They couldn’t let it stop, but it made me aware of something: you could keep adding the same measurement over and over again and gain XP with every time you added it. Therefore, I made sure you couldn’t add a measurement at the same date, and the same time, so you can’t gain XP anymore by only pressing “OK, add this measurement” all the time.

On Thursday (1-2-18) I finished my report, made my ReadMe, tried to obtain a better score on BetterCodeHub.com, and translated this process book to English. I also made the product video today (maybe after 17:59 though).

On Friday (tomorrow) I’m planning on sleeping until noon, then get to the university, check other people’s cool apps and data visualization and then say goodbye to all the nice people I’ve gotten to know during this minor. And celebrate that it’s weekend and that this app is finished, off course!
