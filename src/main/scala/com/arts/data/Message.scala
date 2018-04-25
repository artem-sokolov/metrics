package com.arts.data

sealed abstract class Message {
  val timestamp: Long
}

case class Get(timestamp: Long) extends Message
case class Post(timestamp: Long, username: String, reaction: Reaction) extends Message