# Cuckoo-Android
Social network

# About Cuckoo
This is the simple Android app for the social network that is capable of doing several basic functionalities of existing social networking app such as Instagram, Tinder, Snapchat <br>
This consists of 3 components:
1. Android App: https://github.com/VuNTruong/Cuckoo-Kotlin-Android.git
2. iOS App: https://github.com/VuNTruong/Cuckoo-Swift-iOS.git
3. Server and API: https://github.com/VuNTruong/Cuckoo-Node.js-API.git

This is the Android version of the Cuckoo social network <br>

# Basic functionalities includes:
1. Posting photos (every posts created must have a photo attached to it. Multiple photos can be chosen for a single post) <br>
2. Commenting, Liking posts (photos can be used as comments)
3. Following and unfollowing other users. Once followed, user will see be able to see posts from that followed user in the feed
4. Messaging (photos can be sent as message)
5. Video and audio calling
6. See locations of following users
7. Update location
8. Explore recommended photos (currently working but need more fixes on the backend)
9. Explore users around current location
10. Explore posts around current location
11. See activity summary (see who interact with you the most, who like your posts the most, who comment your posts the most, who visit your profile the most)

# Technologies used:
1. Programming language: Kotlin
2. Design pattern: MVVM (Model - View - View Model)
3. Realtime communication and updates: SocketIO
4. Video and audio calling: Twillio Programmable Video
5. Locations: Mapbox
6. Image recognitions (used when the app need to predict user's search trend): Firebase Machine Learning
7. API operations: Retrofit
8. Notifications: Firebase Cloud Messaging

# Testing out the app
If you want to check out the app, download the app here https://play.google.com/store/apps/details?id=com.beta.myhbt_api 
When testing out the app, if you don't want to create an account, login with this one
Email: ng@email.com
Password: 123456789
