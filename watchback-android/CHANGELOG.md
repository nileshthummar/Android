# v1.0.8

**Release date:** 10/12/2018

#### NOTES:

* Enviornment: PROD
* Version code: 9
* Tagged with 1.0.8

#### Bug Fixes:

* Fixed crashlytics #248:LoginSignupManager:http://crashes.to/s/0028f67e460
* Crashlytics fixes for #245:VideoPlayerActivity:http://crashes.to/s/7e662d80170
* Fixed crashlytics #239:PerkPreferencesManager: http://crashes.to/s/b161e222c75
* Fix NullPointer seen on some devices when lifecycleUtil initialization takes time -delayed the calls slightly in this case. Fixes Crashlytics #249:WatchBackBrightcovePlayer: http://crashes.to/s/9986741ca5b


# v1.0.7

**Release date:** 07/12/2018

#### NOTES:

* Enviornment: PROD
* Version code: 8
* Tagged with 1.0.7

#### New Features, Enhancements, BugFixes:

* Pass access-token from account screen: https://jira.rhythmone.com/browse/PEWAN-435
* Fixed crashlytics #228: WalkthroughActivity IllegalStateException: http://crashes.to/s/975dea8287f
* FB-event tracking for Channels & Interests selection screens: https://jira.rhythmone.com/browse/PEWAN-436
* Added seek-bar for ExoPlayer, made required changes for seeking-action to send corresponding Brightcove-event for callbacks; Made changes needed for getting AYSW prompt to work with ExoPlayer
* Remove max-lines restriction from featured-item description, to allow it to wrap as many as needed: https://jira.rhythmone.com/browse/PEWAN-440
* Do not show 'No video history' toast: https://jira.rhythmone.com/browse/PEWAN-432
* Hide 'Play' icon when showing resume-prompt: https://jira.rhythmone.com/browse/PEWAN-433
* Remove max-lines restriction from featured-item title & description, to allow it to wrap as many as needed: https://jira.rhythmone.com/browse/PEWAN-440
* Fixed crashlytics #231: VideoPlayerActivity:NullPointer: http://crashes.to/s/56c40510744
* Fixed crashlytics #109:PictureInPictureManager:PictureInPictureManagerException: http://crashes.to/s/743c8c7f70e
* Fixed crashlytics #243:LoginSignupManager:BadTokenException: http://crashes.to/s/5d3ef2d37ee


# v1.0.6

**Release date:** 30/11/2018

#### NOTES:

* Enviornment: PROD
* Version code: 7
* Tagged with 1.0.6

#### New Features, Enhancements, BugFixes:

* Modify the app to prevent the splash screen from showing up when app is already running and a protocol-Uri Intent is opened by some action: https://jira.rhythmone.com/browse/PEWAN-392
* Fix issue of Home-screen not being shown after signing-up from unregistered-user screens:  https://jira.rhythmone.com/browse/PEWAN-420
* Added new events for Leanplum:  https://jira.rhythmone.com/browse/PEWAN-398
* FB-event tracking for Search:  https://jira.rhythmone.com/browse/PEWAN-422
* Use provider as subtitle, when short-description is missing or unknown for long-form videos:  https://jira.rhythmone.com/browse/PEWAN-414 
* Fixed inconsistencies with long-form video playback & related events, causing multiple issues. This fixes: https://jira.rhythmone.com/browse/PEWAN-400 / PEWAN-407 / PEWAN-408 / PEWAN-409 / PEWAN-182
* Fix long-form video playback issue seen when trying to play it after a short-form video, for pre-Android 8 devices: https://jira.rhythmone.com/browse/PEWAN-409
* Save playhead positions for multiple long-form videos, depending on the setting-value: https://jira.rhythmone.com/browse/PEWAN-405
* Updated bottom-left featured icon dimensions & visibility, depending on value of 'bottom_left_icon_url': https://jira.rhythmone.com/browse/PEWAN-406
* Display referral id of user on account screen: https://jira.rhythmone.com/browse/PEWAN-427
* Allow description for featured items to span up to maximum 3 lines: https://jira.rhythmone.com/browse/PEWAN-430
* Facebook-event tracking for search: https://jira.rhythmone.com/browse/PEWAN-422
* Direct users to login screen if trying to sign up with an existing Perk account: https://jira.rhythmone.com/browse/PEWAN-417
* Passing long-form video completion percent: https://jira.rhythmone.com/browse/PEWAN-421
* Handling Leanplum URLs for featured content: https://jira.rhythmone.com/browse/PEWAN-428
* Account screen UI updates for showing user point balance: https://jira.rhythmone.com/browse/PEWAN-429
* Facebook-events tracking for Account screen: https://jira.rhythmone.com/browse/PEWAN-431


# v1.0.5

**Release date:** 17/11/2018

#### NOTES:

* Enviornment: PROD
* Version code: 6
* Tagged with 1.0.5

#### New Features, Enhancements, BugFixes:

* Use 'poster' param always for featured-item's images: https://jira.rhythmone.com/browse/PEWAN-403
* Use 'has_gold_border' parameter to enable gold border for featured items and used 'bottom_left_icon_url' to show image from URL in place of the 'featured' tag: https://jira.rhythmone.com/browse/PEWAN-406


# v1.0.4

**Release date:** 07/11/2018

#### NOTES:

* Enviornment: PROD
* Version code: 5
* Tagged with 1.0.4

#### New Features, Enhancements:

* Use ExoPlayer for long-form videos.


# v1.0.3

**Release date:** 30/10/2018

#### NOTES:

* Enviornment: PROD
* Version code: 4
* Tagged with 1.0.3

#### New Features, Enhancements:

* Update Channel/Brand details page 'limit' to 50 for each API call; updated offset to increase accordingly: https://jira.rhythmone.com/browse/PEWAN-388
* Event-tracking updates: Added 'Featured Content Tap' event & custom parameter: https://jira.rhythmone.com/browse/PEWAN-389


# v1.0.2

**Release date:** 24/10/2018

#### NOTES:

* Enviornment: PROD
* Version code: 3
* Tagged with 1.0.2

#### New Features, Enhancements, BugFixes:

* Fix issue of 'Play' button being visible sometimes while video is playing: https://jira.rhythmone.com/browse/PEWAN-374
* Search for related videos when launching a video via 'watchback://video:' protocol: https://jira.rhythmone.com/browse/PEWAN-379
* Save long-form video playHead position every second instead of every 30 seconds: https://jira.rhythmone.com/browse/PEWAN-384
* Limit the number of videos to 20, for each call on brand-portal screen; and increment offset by 'limit' value each time, instead of based on number of videos returned, to prevent duplicates: https://jira.rhythmone.com/browse/PEWAN-381
* Fixed issue of continuous loading screen seen sometimes after login/signup: https://jira.rhythmone.com/browse/PEWAN-377 + Crash-fixes for instances reported in Crashlytics


# v1.0.1

**Release date:** 18/10/2018

#### NOTES:

* Enviornment: PROD
* Version code: 2
* Tagged with 1.0.1

#### New Features, Enhancements, BugFixes:

* AppsFlyer Uninstall tracking: https://jira.rhythmone.com/browse/PEWAN-361
* Update terms, privacy & vppa URLs: https://jira.rhythmone.com/browse/PEWAN-358
* Fixes for Crashlytics issues
* Save the last-played position for the most recently played long-form video: https://jira.rhythmone.com/browse/PEWAN-363
* Add options to 'Resume' or 'Start from beginning' when retrying to play most recent long-form video: https://jira.rhythmone.com/browse/PEWAN-364
* Added Additional Protocol Handlers for Deep Linking: https://jira.rhythmone.com/browse/PEWAN-367
* Implemented caching for Donate, Sweeps carousels and refactored Unregistered-user carousel manager to use common Carousel-Manager: https://jira.rhythmone.com/browse/PEWAN-365
* Updated FB, Singular events: https://jira.rhythmone.com/browse/PEWAN-366


# v1.0.0

**Release date:** 09/10/2018

#### NOTES:

* Enviornment: PROD
* Version code: 1
* Tagged with 1.0.0

#### New Features:

* Initial Release
