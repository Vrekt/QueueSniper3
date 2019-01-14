# QueueSniper3
A discord bot for hosting Fortnite snipe matches.

# Features

A list of features and commands:
  - A configuration for managing channels, roles, etc.
  - A stats counterpart where players can link their Fortnite accounts.
  - Start and cancel snipe matches
  - An organized and pretty embed that sorts codes and shows their Fortnite account name + mention
  - and more!
# Commands

Commands (Note default prefix is '.', but you can change this with .config):
  - ``!cancel <gamemode>`` - Cancels a match that has started collecting codes.
  - ``!start <gamemode> (optional) #channel`` - Starts a snipe match, if you specify a channel then all embeds will go there.
  - ``!lock`` - Locks a channel
  - ``!unlock`` - Unlocks a channel
  - ``!link <platform> <name>`` - Allows players to link their Fortnite accounts, this is not verified.
  - ``!unlink`` - Allows players to unlink their Fortnite account.
  - If you are an administrator you can unlink other players account like this ``!unlink @member``.
  - ``!stats`` - Allows a player to view their stats.
  - ``!stats @player`` - Allows a player to view another players stats.
 
 Here are some screenshots:
 
![alt text](https://i.imgur.com/Xrztynt.png)
 
![alt text](https://i.imgur.com/waHHY1N.png)
 
![alt text](https://i.imgur.com/LN6xKiY.png)
 
![alt text](https://i.imgur.com/aN94sad.png)
 
 # Adding QueueSniper to your discord

You can add it to your discord [here](https://discordapp.com/oauth2/authorize?&client_id=513096941693960223&scope=bot)

Make sure it has send permissions first time, if the bot is not responding at all this is 99.9% a permission problem.

 - Once it is in your server type ".setup", the bot will guide you through setting up.
 - Once that is finished you're all set! If you're afraid the bot does not have proper permissions type any command,
 - you will see a warning prompt if it does not have all permissions required.

# Self hosting

Self hosting requires JDK 11. You can find it [here](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)

Once you have JDK 11 installed verify its the correct version by running ``java -version``, if it says something other than ``11.x.x`` then try and uninstall the previous java versions and reinstall JDK 11.

Now, head over to the releases page [here](https://github.com/Vrekt/QueueSniper3/releases) and grab the latest JAR.

Next, get your discord token and you will also need your epic games username/password, this is for the statistics API.
(NOTE: If you do not want to provide a username and password you can just type random characters in those fields.
However as a result link/unlink/stats commands will be disabled)

Then, run the file with these arguments ``java -jar QueueSniper.jar <discord token> <username> <password>``

For example if you wanted to run it without a username and password ``java -jar QueueSniper.jar MYTOKEN a a``
