package com.esyfur.rql

import scala.collection.JavaConverters._

import com.rethinkdb.{Ql2 => p}
import p.Datum.DatumType.{R_NULL, R_BOOL, R_NUM, R_STR, R_ARRAY, R_OBJECT}

object Datum {

    def apply(value: Any): Datum[Any] = value match {
        case null                => new NullDatum
        case v: Boolean          => new BoolDatum(v)
        case v: Int              => new NumDatum(v)
        case v: Long             => new NumDatum(v)
        case v: Float            => new NumDatum(v)
        case v: Double           => new NumDatum(v)
        case v: String           => new StrDatum(v)
        case v: Seq[Any]         => new ArrayDatum(v)
        case v: Map[String, Any] => new ObjectDatum(v)
        case _ => {
            val message = "Cannot convert %s to datum.".format(value.getClass)
            throw new RqlDriverError(message)
        }
    }

    def unwrap(datum: p.Datum): Any = datum.getType match {
        case R_NULL    => null
        case R_BOOL    => datum.getRBool
        case R_NUM     => datum.getRNum
        case R_STR     => datum.getRStr
        case R_ARRAY   => ArrayDatum unwrap datum.getRArrayList.asScala
        case R_OBJECT  => ObjectDatum unwrap datum.getRObjectList.asScala
        case datumType => {
            val message = "Unexpected datum type %s.".format(datumType)
            throw new RqlDriverError(message)
        }
    }

}

private[rql] abstract class Datum[+T] extends Term {

    protected val termType = p.Term.TermType.DATUM
    protected val datumType: p.Datum.DatumType

    protected val value: T

    protected override def getTermBuilder() = super.getTermBuilder().setDatum(getDatumBuilder)

    protected def getDatumBuilder(): p.Datum.Builder = p.Datum.newBuilder().setType(datumType)

}

final class NullDatum extends Datum[Null] {

    protected val datumType = R_NULL
    protected val value = null

}

final class BoolDatum(val value: Boolean) extends Datum[Boolean] {

    protected val datumType = R_BOOL

    protected override def getDatumBuilder() = super.getDatumBuilder().setRBool(value)

}

final class NumDatum(val value: Double) extends Datum[Double] {

    protected val datumType = R_NUM

    protected override def getDatumBuilder() = super.getDatumBuilder().setRNum(value)

}

final class StrDatum(val value: String) extends Datum[String] {

    protected val datumType = R_STR

    protected override def getDatumBuilder() = super.getDatumBuilder().setRStr(value)

}

object ArrayDatum {

    def unwrap(datum: Seq[p.Datum]): Seq[Any] = datum.map(Datum unwrap _)

}

/**
 * ArrayDatum isn't actually a datum, but a MAKE_ARRAY query.
 */
final class ArrayDatum(val value: Seq[Any]) extends Datum[Seq[Any]] {

    protected override val termType = p.Term.TermType.MAKE_ARRAY //
    protected val datumType = R_ARRAY

    protected override def getTermBuilder() = {
        val builder = super.getTermBuilder().clearDatum()

        // TODO:

        builder
    }

}

object ObjectDatum {

    def unwrap(datum: Seq[p.Datum.AssocPair]): Map[String, Any] = datum.map(pair =>
        pair.getKey -> (Datum unwrap pair.getVal)
    )(collection.breakOut)

}

/**
 * ObjectDatum isn't actually a datum, but a MAKE_OBJ query.
 */
final class ObjectDatum(val value: Map[String, Any]) extends Datum[Map[String, Any]] {

    protected override val termType = p.Term.TermType.MAKE_OBJ
    protected val datumType = R_OBJECT

    protected override def getTermBuilder() = {
        val builder = super.getTermBuilder().clearDatum()

        for ((key, arg) <- value) {
            val pair = p.Term.AssocPair.newBuilder
                .setKey(key)
                .setVal(expr(arg).build)

            builder.addOptargs(pair)
        }

        builder
    }

}
