package com.arts.data

sealed trait Reaction

case object Click extends Reaction
case object Impression extends Reaction
