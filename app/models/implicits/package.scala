package models

package object implicits {
	implicit val wrappedRSToUser = User.fromRS _
	implicit val wrappedRSToInstrument = Instrument.fromRS _
}