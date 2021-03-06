package com.beta.cuckoo.Repository.MessageRepositories

import android.content.Context
import com.beta.cuckoo.Network.Messages.*
import com.beta.cuckoo.Network.RetrofitClientInstance
import com.beta.cuckoo.Model.Message
import com.beta.cuckoo.Model.MessagePhoto
import com.beta.cuckoo.Model.MessageRoom
import com.beta.cuckoo.Network.VideoChat.DeleteVideoChatRoomService
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.bumptech.glide.Glide
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class MessageRepository (executor: Executor, context: Context) {
    // These objects are used for socket.io
    //private lateinit var mSocket: Socket
    private val gson = Gson()

    // The executor to do work in background thread
    private val executor = executor

    // Context of the parent activity
    private val context = context

    // The user repository (to get info of the currently logged in user for some uses)
    private val userRepository = UserRepository(executor, context)

    // The function to get list of message rooms in which currently logged in user is in
    fun getListOfMessageRoomsOfCurrentUser (callback: (messageRooms: ArrayList<MessageRoom>) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser {userObject ->
                // Create the get message room of user service
                val getMessageRoomOfUserService: GetMessageRoomOfUserService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    GetMessageRoomOfUserService::class.java)

                // Create the call object in order to perform the call
                val call: Call<Any> = getMessageRoomOfUserService.getMessageRoomOfUserService(userObject.getId())

                // Perform the call
                call.enqueue(object: Callback<Any> {
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        print("Boom")
                    }

                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        // If the response body is not empty, there is data
                        if (response.body() != null) {
                            // Array of message rooms
                            val arrayOfMessageRooms = ArrayList<MessageRoom>()

                            // Body of the request
                            val responseBody = response.body() as Map<String, Any>

                            // Get data from the response body (array of message rooms)
                            val data = responseBody["data"] as List<Map<String, Any>>

                            // Loop through that array of message room to create objects out of those info and add them to array of message rooms
                            for (messageRoom in data) {
                                // Get id of the message room
                                val messageRoomId = messageRoom["_id"] as String

                                // Get user1 id
                                val user1Id = messageRoom["user1"] as String

                                // Get user2 id
                                val user2Id = messageRoom["user2"] as String

                                // Create object out of those info
                                val messageRoomObject = MessageRoom(user1Id, user2Id, messageRoomId)

                                // Add the new message room object to the array of message rooms
                                arrayOfMessageRooms.add(messageRoomObject)

                                // Return array of message rooms via callback function
                                callback(arrayOfMessageRooms)
                            }
                        } else {
                            print("Something is not right")
                        }
                    }
                })
            }
        }
    }

    // The function to get latest message in the message room
    fun getLatestMessageInMessageRoom (messageRoomId: String, callback: (latestMessageContent: String) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser {userObject ->
                // Create the get latest message of message room service
                val getLatestMessageOfMessageRoomService: GetLatestMessageOfMessageRoomService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    GetLatestMessageOfMessageRoomService::class.java)

                // Create the call object in order to perform the call
                val call: Call<Any> = getLatestMessageOfMessageRoomService.getLatestMessageOfMessageRoom(messageRoomId)

                // Perform the call
                call.enqueue(object: Callback<Any> {
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        print("Boom")
                    }

                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        // If the response body is not empty it means that there is no error
                        if (response.body() != null) {
                            // Body of the request
                            val responseBody = response.body() as Map<String, Any>

                            if (responseBody["data"] != null) {
                                // Get data from the response body
                                val data = responseBody["data"] as Map<String, Any>

                                // Get content of the latest message
                                val latestMessageContent = data["content"] as String

                                // Get sender of the latest message
                                val latestMessageSender = data["sender"] as String

                                // Check to see if latest message is written by the current user or not
                                if (latestMessageSender == userObject.getId()) {
                                    // Return content of the latest message via callback function
                                    // Also let the user know that it is sent by the current user
                                    callback("You: $latestMessageContent")
                                } // Otherwise, just load the content in
                                else {
                                    callback(latestMessageContent)
                                }
                            }
                        } else {
                            callback("")
                        }
                    }
                })
            }
        }
    }

    // The function to load all messages of the selected message room
    fun getMessagesOfMessageRoom (messageRoomId: String, callback: (messages: ArrayList<Message>) -> Unit) {
        executor.execute {
            // Create the get messages service
            val getAllMessagesService: GetAllMessagesOfChatRoomService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                GetAllMessagesOfChatRoomService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getAllMessagesService.getAllMessagesOfChatRoom(messageRoomId)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that the token is valid
                    if (response.body() != null) {
                        // Body of the response
                        val responseBody = response.body() as Map<String, Any>

                        // Get data from the response body
                        val data = responseBody["data"] as Map<String, Any>

                        // Get all messages from the data
                        val messages = data["documents"] as ArrayList<Message>

                        // Return array of messages via callback function
                        callback(messages)
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }

    // The function to get message object based on message id
    fun getMessageObjectBasedOnMessageId (messageId: String, callback: (messageObject: Message) -> Unit) {
        executor.execute {
            // Create the get message object based on message id service
            val getMessageObjectBasedOnMessageIdService: GetMessageObjectBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                GetMessageObjectBasedOnIdService::class.java
            )

            // Create the call object in order to perform the call
            val call: Call<Any> = getMessageObjectBasedOnMessageIdService.getMessageObjectBasedOnId(messageId)

            // Perform the call
            call.enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that the token is valid
                    if (response.body() != null) {
                        // Body of the response
                        val responseBody = response.body() as Map<String, Any>

                        // Get data from the response body
                        val data = responseBody["data"] as Map<String, Any>

                        // Get all messages from the data
                        //val messages = (data["documents"] as ArrayList<Message>)[0]

                        // Convert the user info data object which is currently a linked tree map into a JSON string
                        val js = gson.toJson((data["documents"] as ArrayList<Message>)[0])

                        // Convert the JSON string back into Message class
                        val messageModel = gson.fromJson<Message>(js, Message::class.java)

                        // Return array of messages via callback function
                        callback(messageModel)
                    } else {
                        print("Something is not right")
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Something is not right")
                }
            })
        }
    }

    // The function to send message from a currently logged in user to the specified message receiver
    fun sendMessage (messageRoomId: String, messageReceiverUserId: String, messageContent: String, callback: (messageSentFirstTime: Boolean, messageObject: Message, chatRoomId: String, currentUserId: String, messageSentStatus: String) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the create new messages service
                val createNewMessageService: CreateNewMessageService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    CreateNewMessageService::class.java)

                // Create the call object in order to perform the call
                val call: Call<Any> = createNewMessageService.createNewMessage(userObject.getId(), messageReceiverUserId, messageContent)

                // Perform the call
                call.enqueue(object: Callback<Any> {
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        print("Boom")
                    }

                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        // If the response body is not empty it means that message is created
                        if (response.body() != null) {
                            // Body of the response
                            val responseBody = response.body() as Map<String, Any>

                            // Check the status
                            val status = responseBody["status"] as String

                            // If status is not "Message created", there is something wrong between these 2 users
                            if (status != "Message created") {
                                // Let the view know that message is not sent and also notify the status
                                callback(false, Message("", "", "", ""), "", "", status)
                            } else {
                                // Get data from the response body (message object of the newly created message)
                                val data = responseBody["data"] as Map<String, Any>

                                // Create the new message object
                                val newMessageObject = Message(userObject.getId(), messageReceiverUserId, messageContent, data["_id"] as String)

                                // If message is sent for the first time, call the function to set up socket.io for realtime update
                                // if this happens, chat room id of this activity will be empty. We need to check it
                                if (messageRoomId == "") {
                                    // Set the current chat room to be the one that contain the message that just been sent
                                    // This is very important especially when message is sent at the first time
                                    callback(true, newMessageObject, data["chatRoomId"] as String, userObject.getId(), "Sent")
                                } else {
                                    // If room is already set, just need to return newly created message object
                                    callback(false, newMessageObject, data["chatRoomId"] as String, userObject.getId(), "Sent")
                                }
                            }
                        } else {
                            print("Something is not right")
                        }
                    }
                })
            }
        }
    }

    // The function to create new message image in the database based on image id and image URL
    fun createNewMessageImage (messageId: String, imageURL: String, callback: () -> Unit) {
        executor.execute {
            // Create the create chat message photo service
            val createMessagePhotoService: CreateNewChatMessagePhotoService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                CreateNewChatMessagePhotoService::class.java)

            // The call object which will then be used to perform the API call
            val call: Call<Any> = createMessagePhotoService.createMessagePhoto(messageId, imageURL)

            // Perform the API call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    // Report the error if something is not right
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // Call the function to let the view model know that message image has been uploaded to the database
                    callback()
                }
            })
        }
    }

    // The function to get message image based on message id
    fun getMessageImageBasedOnId (messageId: String, callback: (messageImage: MessagePhoto) -> Unit) {
        executor.execute {
            // Create the get message photo based on message id service
            val getMessagePhotoBasedOnMessageIdService : GetChatMessagePhotoService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                GetChatMessagePhotoService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getMessagePhotoBasedOnMessageIdService.getMessagePhoto(messageId)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that there is no error
                    if (response.body() != null) {
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get data from the response body
                        val data = responseBody["data"] as Map<String, Any>

                        // Get image info from the data
                        val imageInfo = (data["documents"] as List<Map<String, Any>>)[0]

                        // Convert the data object which is currently a linked tree map into a JSON string
                        val js = gson.toJson((data["documents"] as List<Map<String, Any>>)[0])

                        // Convert the JSOn string back into MessagePhoto class
                        val messagePhotoModel = gson.fromJson<MessagePhoto>(js, MessagePhoto::class.java)

                        // Get image URL of the message photo
                        //val imageURL = imageInfo["imageURL"] as String

                        // Return image URL to the view via callback function
                        callback(messagePhotoModel)
                    }
                }
            })
        }
    }

    // The function to check for chat room between the 2 users
    // (between currently logged in user and other user with specified id)
    fun checkChatRoomBetween2Users (otherUserId: String, callback: (chatRoomId: String) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the get chat room between 2 users service
                val getChatRoomIdBetween2UsersService: GetMessageRoomIdBetween2UsersService =
                    RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                        GetMessageRoomIdBetween2UsersService::class.java
                    )

                // Create the call object in order to perform the call
                val call: Call<Any> =
                    getChatRoomIdBetween2UsersService.getMessageRoomIddBetween2Users(
                        userObject.getId(),
                        otherUserId
                    )

                // Perform the call
                call.enqueue(object : Callback<Any> {
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        print("There seem be be an error ${t.stackTrace}")
                    }

                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        // Check response body
                        if (response.body() != null) {
                            // Body of the request
                            val responseBody = response.body() as Map<String, Any>

                            // Get status of the call (it can be either empty or success)
                            val status = responseBody["status"] as String

                            // If the status is success, get message room id and pass it to the next view controller
                            if (status == "success") {
                                // Get data of the response
                                val data = responseBody["data"] as Map<String, Any>

                                // Chat chat room id
                                val chatRoomId = data["_id"] as String

                                // Return chat room id via callback function
                                callback(chatRoomId)
                            } // Otherwise, go to the chat activity and let the chat room id be blank
                            else {
                                callback("")
                            }
                        } else {
                            print("Something is not right")
                        }
                    }
                })
            }
        }
    }

    // The function to get list of photos in chat room with specified id
    fun getPhotosOfChatRoom (chatRoomId: String, callback: (arrayOfPhotos: ArrayList<MessagePhoto>) -> Unit) {
        executor.execute {
            // Create the get photos of chat room service
            val getPhotosOfChatRoomService: GetMessagePhotosOfChatRoomService =
                RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    GetMessagePhotosOfChatRoomService::class.java
                )

            // Create the call object in order to perform the call
            val call: Call<Any> =
                getPhotosOfChatRoomService.getMessagePhotosOfChatRoom(chatRoomId)

            // Perform the call
            call.enqueue(object : Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("There seem be be an error ${t.stackTrace}")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // Check response body
                    if (response.body() != null) {
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get data of the response (array of photo URL between users)
                        val arrayOfPhotoURL = responseBody["data"] as ArrayList<MessagePhoto>

                        // Return array of photo URL to view via callback function
                        callback(arrayOfPhotoURL)
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }

    // The function to create new message room
    fun createNewMessageRoom (user1: String, user2: String, callback: (messageRoomId: String) -> Unit) {
        executor.execute {
            // Create the create new message room service
            val createNewMessageRoomService: CreateNewMessageRoomService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(CreateNewMessageRoomService::class.java)

            // Create the call object to perform the call
            val call: Call<Any> = createNewMessageRoomService.createNewMessageRoom(user1, user2)

            // Perform the call
            call.enqueue(object : Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("There seem be be an error ${t.stackTrace}")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // Check response body
                    if (response.body() != null) {
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get data of the response
                        val responseData = responseBody["data"] as Map<String, Any>

                        // Get newly created chat room info
                        val newChatRoomInfo = responseData["tour"] as Map<String, Any>

                        // Get chat room id of the newly created chat room
                        val chatRoomId = newChatRoomInfo["_id"] as String

                        // Return chat room id of the newly created chat room to view via callback function
                        callback(chatRoomId)
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }

    // The function to delete a chat room with specified chat room id
    fun deleteChatRoom (chatRoomId: String, callback: (isDeleted: Boolean) -> Unit) {
        executor.execute {
            // Create the delete chat room service
            val deleteChatRoomService: DeleteChatRoomService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(DeleteChatRoomService::class.java)

            // Create the call object to perform the call
            val call: Call<Any> = deleteChatRoomService.deleteChatRoom(chatRoomId)

            // Perform the call
            call.enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If response body is not null, it means that chat room has been removed
                    if (response.body() != null) {
                        // Let view know that chat room has been removed via callback function
                        callback(true)
                    } // Otherwise, let the view know that chat room has not been deleted
                    else {
                        callback(false)
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Something is not right")
                    callback(false)
                }
            })
        }
    }
}