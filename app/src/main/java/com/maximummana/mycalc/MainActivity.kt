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
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    private var inputData = StringBuilder()
    private var historyData = StringBuilder()

    /**
     * Сохраняет данные (P.s. знаю можно и ViewModel
     */
    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)

        state.putString("inputData", inputData.toString())
        state.putString("historyData", historyData.toString())
    }

    /**
     * Востанавливет данные (P.s. знаю можно и ViewModel
     */
    override fun onRestoreInstanceState(state: Bundle) {
        super.onRestoreInstanceState(state)

        inputData.clear()
        inputData.append(state.getString("inputData"))
        binding.field.text = inputData

        historyData.clear()
        historyData.append(state.getString("historyData"))
        binding.history.text = historyData
    }

    /**
     * Делает текст прокручиваемым
     * и вешает слушатели на кнопки
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.history.movementMethod = ScrollingMovementMethod()
        for (i in binding.keyboard.root.children) i.setOnClickListener(this)
    }

    /**
     * Каждой кнопке клавиатуры устанавливет действие
     * @param v View? должен быть кнопкой
     */
    override fun onClick(v: View?) {
        if (v is Button) {
            when (v.id) {
                R.id.buttonClearAll -> {
                    inputData.clear()
                }

                R.id.buttonClearBack -> {
                    if (inputData.isNotEmpty()) {
                        inputData.deleteAt(inputData.lastIndex)
                    }
                }

                R.id.buttonCalculate -> {
                    try {
                        historyData.append("$inputData = ${CalcCore.calculate(inputData.toString())}\n")
                        inputData.clear()
                        binding.history.text = historyData
                    } catch (e: Exception) {
                        Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }

                //0..9 and operators
                else -> inputData.append(v.text)
            }
            binding.field.text = inputData
        }
    }
}

/**
 * @author man-y
 * Простой класс для вычисления математических выражений.
 */
object CalcCore {

    /**
     * Обертка для преобразования и вычислений
     * @param input String входное математическое выражение (не преобразованное)
     * @return результат вычислений
     */
    fun calculate(input: String) = counting(getFormat(input))


    /**
     * Готовит мат. выражение к вычислениям
     * @throws Exception c разными сообщениями связанными с ошибками в входной строке
     * @param input String входное математическое выражение (не преобразованное)
     * @return строку в обратной польской записи
     */
    private fun getFormat(input: String): String {

        if (!checkBrackets(input)) throw Exception("Error in bracket")

        val output = StringBuilder()
        val operatorsStack = Stack<Char>()
        var i = 0


        while (i < input.length) {

            if (Character.isDigit(input[i])) {
                while (input[i] !in "+-/*√()") {
                    output.append(input[i])
                    i++
                    if (i == input.length) break
                }
                output.append(" ")
                i--
            }


            if (input[i] in "+-/*√()") {
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
                        when {
                            input[i] == '√' && (i == 0 || input[i - 1] in "+-м/*()") -> {
                                changeUnaryOperatorPos("к", operatorsStack, output)
                                operatorsStack.push('к')
                            }
                            input[i] == '-' && (i == 0 || input[i - 1] in "+/*√к()") -> {
                                changeUnaryOperatorPos("м", operatorsStack, output)
                                operatorsStack.push('м')
                            }
                            else -> {
                                changeOperatorPos(input[i].toString(), operatorsStack, output)
                                operatorsStack.push(input[i])
                            }
                        }
                    }
                }
            }
            i++
        }

        while (operatorsStack.size > 0) output.append(operatorsStack.pop()).append(" ")
        return output.toString()
    }

    /**
     * Раставляет опирации согласно приоритету
     * @param operator текущий оператор
     * @param operatorsStack текущий стек с опирациями
     * @param output String собираемое математическое выражение (в обратной польской записи)
     * @return true если все без ошибок
     */
    private fun changeOperatorPos(operator:String, operatorsStack:Stack<Char>,output:StringBuilder) {
        if (operatorsStack.size > 0) {
            if (getPriority(operator) <= getPriority(operatorsStack.peek().toString())) {
                output.append(operatorsStack.pop().toString()).append(" ")
            }
        }
    }

    /**
     * Спец. версия для операций с одним аргументом. Раставляет опирации согласно приоритету
     * @param operator текущий оператор
     * @param operatorsStack текущий стек с опирациями
     * @param output String собираемое математическое выражение (в обратной польской записи)
     * @return true если все без ошибок
     */
    private fun changeUnaryOperatorPos(operator:String, operatorsStack:Stack<Char>,output:StringBuilder) {
        if (operatorsStack.size > 0) {
            if (getPriority(operator) < getPriority(operatorsStack.peek().toString())) {
                output.append(operatorsStack.pop().toString()).append(" ")
            }
        }
    }

    /**
     * Готовит мат. выражение к вычислениям
     * @param input String входное математическое выражение (в обратной польской записи)
     * @return double значение выражения
     */
    private fun counting(input: String): Double {
        var result = 0.0
        val temp = Stack<Double>()

        val data = input.dropLast(1).split(" ")
        for (e in data) {
            if (isOperator(e)) {
                if (temp.empty()) throw Exception("Only operator")
                val a = temp.pop()
                when (e) {
                    "к" -> {
                        if (a < 0) throw Exception("Sqrt of a negative number")
                        result = sqrt(a)
                    }
                    "м" -> {
                        result = -a
                    }
                    else -> {

                        if (temp.empty()) throw Exception("Only one number")
                        val b = temp.pop()

                        when (e) {
                            "+" -> result = b + a
                            "-" -> result = b - a
                            "*" -> result = b * a
                            "/" -> {
                                if (a == 0.0) throw Exception("Division by zero")
                                result = b / a
                            }
                            "√" -> {
                                if (a < 0) throw Exception("Sqrt of a negative number")
                                result = b * sqrt(a)
                            }
                        }
                    }
                }
                temp.push(result)
            } else
                temp.push(e.toDouble())
        }
        return temp.peek()
    }

    /**
     * Проверка на правильность скобок
     * @param input String входное математическое выражение (не преобразованное)
     * @return true если все без ошибок
     */
    private fun checkBrackets(input: String): Boolean {
        var brackets = 0
        for (i in input) {
            if (i == '(') brackets++
            if (i == ')') brackets--
            if (brackets < 0) return false
        }
        return brackets == 0
    }

    /**
     * Проверка на оператор
     * @param s String оператор
     * @return true если это был оператор
     */
    private fun isOperator(s: String) = s in "+-м/*√к()"

    /**
     * Устанавливает приоритетность операции
     * @param s String оператор
     * @return int приоритет
     */
    private fun getPriority(s: String) = when (s) {
        "(" -> 0
        ")" -> 1
        "+" -> 2
        "-" -> 3
        "*", "/" -> 4
        "√" -> 5
        "м","к" -> 6
        else -> 7
    }
}