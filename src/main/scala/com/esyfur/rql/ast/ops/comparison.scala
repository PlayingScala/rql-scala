package com.esyfur.rql.ast.ops

import com.rethinkdb.{Ql2 => p}
import com.esyfur.rql.{Value, BiOpQuery}

class Eq(a: Value, b: Value) extends BiOpQuery(a, b) {

    protected val termType = p.Term.TermType.EQ
    val st = "=="

}

class Ne(a: Value, b: Value) extends BiOpQuery(a, b) {

    protected val termType = p.Term.TermType.NE
    val st = "!="

}

class Lt(a: Value, b: Value) extends BiOpQuery(a, b) {

    protected val termType = p.Term.TermType.LT
    val st = "<"

}

class Le(a: Value, b: Value) extends BiOpQuery(a, b) {

    protected val termType = p.Term.TermType.LE
    val st = "<="

}

class Gt(a: Value, b: Value) extends BiOpQuery(a, b) {

    protected val termType = p.Term.TermType.GT
    val st = ">"

}

class Ge(a: Value, b: Value) extends BiOpQuery(a, b) {

    protected val termType = p.Term.TermType.GE
    val st = ">="

}
