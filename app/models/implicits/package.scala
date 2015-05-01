package models

import scalikejdbc.WrappedResultSet

package object implicits {

	implicit val wrappedRSToUser = User.async.fromRS _
	implicit val wrappedRSToInstrument = Instrument.async.fromRS _
	implicit val wrappedRSToTuning = Tuning.async.fromRS _

	implicit val wrappedRSToInt = { (rs: WrappedResultSet) => rs.int(0) }

	implicit val jsFormatUser = User.jsFormat
	implicit val jsFormatScale = Scale.jsFormat
	implicit val jsFormatInstrument = Instrument.jsFormat
	implicit val jsFormatTuning = Tuning.jsFormat
}