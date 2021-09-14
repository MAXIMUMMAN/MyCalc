package com.maximummana.mycalc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.view.children
import com.maximummana.mycalc.databinding.ActivityMainBinding
import java.lang.Exception
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt
import android.R.bool





class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private var inputData = lazy { StringBuilder() }
    private var historyData = lazy { StringBuilder() }

    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)
        state.putString("inputData", inputData.value.toString())
        state.putString("historyData", historyData.value.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        inputData.value.clear()
        inputData.value.append(savedInstanceState.getString("inputData"))
        binding.field.text = inputData.value
        historyData.value.clear()
        historyData.value.append(savedInstanceState.getString("historyData"))
        binding.history.text = historyData.value
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.history.movementMethod = ScrollingMovementMethod()
        for (i in binding.keyboard.root.children) i.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        try {
            if (v is Button)
                when (v.id) {

                    R.id.button1 -> inputData.value.clear()

                    R.id.button3 -> if (inputData.value.isNotEmpty()) {
                        inputData.value.deleteAt(inputData.value.lastIndex)
                    }

                    R.id.button4 -> {
                        historyData.value.append("${inputData.value} = ${Core.calculate(inputData.value.toString())}\n")
                        inputData.value.clear()
                        binding.history.text = historyData.value
                    }

                    else -> inputData.value.append(v.text)
                }
            binding.field.text = inputData.value

        } catch (e: Exception) {
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}


object Core {

    fun calculate(input: String) = counting(getFormat(input))

    private fun getFormat(input: String): String {

        if(!checkBrackets(input)) throw Exception("Error in bracket")

        val output = StringBuilder()
        val operatorsStack = Stack<Char>()
        var i = 0
        while (i < input.length) {

            if (isDelimiter(input[i])) {
                i++
                continue
            }

            if (Character.isDigit(input[i])) {
                while (!isDelimiter(input[i]) && !isOperator(input[i])) {
                    output.append(input[i])
                    i++
                    if (i == input.length) break
                }
                output.append(" ")
                i--
            }


            if (isOperator(input[i])) {
                when {
                    input[i] == '(' -> operatorsStack.push(input[i])
                    input[i] == ')' -> {
                        var s = operatorsStack.pop()
                        while (s != '(') {
                            output.append("$s ")
                            s = operatorsStack.pop()
                        }
                    }
                    else -> {
                        if (operatorsStack.size > 0) {
                            if (getPriority(input[i]) <= getPriority(operatorsStack.peek())) {
                                output.append(operatorsStack.pop().toString()).append(" ")
                            }
                        }
                        operatorsStack.push(input[i])
                    }
                }
            }
            i++
        }

        while (operatorsStack.size > 0) output.append(operatorsStack.pop()).append(" ")
        return output.toString()
    }

    private fun counting(input: String): Double {
        var result = 0.0
        val temp = Stack<Double>()
        var i = 0
        while (i < input.length) {

            if (Character.isDigit(input[i])) {
                var a = ""
                while (!isDelimiter(input[i]) && !isOperator(input[i])) {
                    a += input[i]
                    i++
                    if (i == input.length) break
                }
                temp.push(a.toDouble())
                i--
            } else
                if (isOperator(input[i])) {
                    if(temp.empty()) throw Exception("Only operator")
                    val a = temp.pop()
                    if(temp.empty()) throw Exception("Only one number")
                    val b = temp.pop()
                    when (input[i]) {
                        '+' -> result = b + a
                        '-' -> result = b - a
                        '*' -> result = b * a
                        '/' -> {
                            if(a == 0.0) throw Exception("Division by zero")
                            result = b / a
                        }
                        '√' -> {
                            if(a < 0) throw Exception("Sqrt of a negative number")
                            result = b * sqrt(a)
                        }
                    }
                    temp.push(result)
                }
            i++
        }
        return temp.peek()
    }

    private fun checkBrackets(input: String): Boolean {
        var brackets = 0
        for (i in input) {
            if (i == '(') brackets++
            if (i == ')') brackets--
            if (brackets < 0) return false
        }
        return brackets == 0
    }

    private fun isDelimiter(c: Char) = c in " "

    private fun isOperator(c: Char) = c in "+-/*√()"

    private fun getPriority(s: Char) =
        when (s) {
            '(' -> 0
            ')' -> 1
            '+' -> 2
            '-' -> 3
            '*' -> 4
            '/' -> 4
            '√' -> 5
            else -> 6
        }
}