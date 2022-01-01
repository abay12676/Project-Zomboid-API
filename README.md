# Project Zomboid API
This is an unofficial Java API for PZ. This can be used for exploit mitigation, creating advanced new features (networking), etc.

## Anti-Cheat
This API comes with a small anti-cheat for your server, as the game is missing authentication on various things. In order to configure the anti-cheat, place a json file called ```anticheat.json``` in the directory with your game files.

```Json
{
	"discordApi": "",
	"enforceSyncPerks": false,
	"enforceSyncThreshold": 10,
	"enforceTeleport": false,
	"enforceExtraInfo": false,
	"enforcePlayerDeaths": false,
	"enforceAdditionalPain": false,
	"rateLimiting": true,
	"rateLimits": [
		{
			"type": "StartFire",
			"delay": 100
		}
	]
}
```

### discordApi
This the URL to a discord webhook so that you can receive notifications when a player receives a violation.

### enforceSyncPerks
This option will ensure that values in the sync perk packet do not exceed a certain amount in one change.

### enforceSyncThreshold
This is the maximum value that a perk can change by in one update.

### enforceTeleport
This option will ensure that nobody teleports except mods and admins. This packet is not sent under normal circumstances, so it is safe to block it.

### enforceExtraInfo
This option will ensure that nobody changes their admin privileges. This packet is not sent under normal circumstances, so it is safe to block it.

### enforcePlayerDeaths
This option will ensure that nobody sends deaths for _other players_. This packet is not sent for other players under normal circumstances, so it is safe to block it.

### enforceAdditionalPain
This option will ensure that nobody sends additional pain packets. This packet is not sent under normal circumstances, so it is safe to block it.

### rateLimiting
This option will apply rate limiting to packets to stop mass griefing attacks, spawning fires, mass killing, etc.

## Installation
* Create a jar file from the root of your PZ, name this `zombie.jar`, and place it in the `lib` directory
* Compile the API into a runnable jar with __Java 15__
* Place this jar into your PZ root directory
* Run the jar file with the `-install` parameter on the command line
* Your server/client is now modded