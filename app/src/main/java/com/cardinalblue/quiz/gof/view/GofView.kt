// Copyright Apr 2018-present CardinalBlue
//
// Author: boy@cardinalblue.com
//
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.

package com.cardinalblue.quiz.gof.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.*

class GofView : View {
    private var paint: Paint
    private val step : Float
    lateinit var mPoints: FloatArray

    //First draw
    private var isFrist : Boolean

    lateinit var tab: Array<FloatArray>
    lateinit var nextTab: Array<FloatArray>
    private val rowSize : Int
    private val cellSize : Int
    private val lifeGame : LifeGame

    init {
        // Paint
        paint = Paint()
        setupPaint()
        // 點間距
        step = 15f

        isFrist = true

        rowSize = 100
        cellSize = 300
        lifeGame = LifeGame(rowSize, cellSize)
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        // TODO: Finish the implementation
    }

    private fun setupPaint() {
        //Paint.style – 繪製模式
        //paint.setColor(int Color) – 顏色
        //Paint.strokeWidth – 線條寬度
        //Paint.isAntiAlias – 抗鋸齒開關
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.setStyle(Paint.Style.FILL_AND_STROKE)
        paint.setColor(Color.BLACK)
        paint.setStrokeWidth(15f)
    }
    fun ClosedRange<Int>.random() =
            Random().nextInt((endInclusive + 1) - start) +  start

    open class LifeGame(rowSize: Int, cellSize: Int) {
        //First array to record points
        var tab: Array<FloatArray> = Array(rowSize) { FloatArray(cellSize) }

        fun nextTab(tab: Array<FloatArray>): Array<FloatArray> {
            //Next generation array
            val nextTab = Array(tab.size) { FloatArray(tab[0].size) }
            //point numbers which is around center point
            var count: Int

            /*rule:
            1.Any live cell with fewer than two live neighbors dies, as if by under population.
            2.Any live cell with two or three live neighbors lives on to the next generation.
            3.Any live cell with more than three live neighbors dies, as if by overpopulation.
            4.Any dead cell with exactly three live neighbors becomes a live cell, as if by reproduction.
            */
            for (i in tab.indices) {
                for (j in 0 until tab[i].size) {
                    count = 0
                    //check 3 by 3 grid
                    for (x in -1..1) {
                        for (y in -1..1) {
                            if (i + x >= 0 && j + y >= 0 && i + x < tab.size && j + y < tab[i].size && !(x == 0 && y == 0)) {
                                count += tab[i + x][j + y].toInt()
                            }
                        }
                    }
                    if (count == 2) {
                        //same
                        nextTab[i][j] = tab[i][j]
                    } else if (count == 3) {
                        //alive
                        nextTab[i][j] = 1f
                    } else {
                        //dead
                        nextTab[i][j] = 0f
                    }
                }
            }

            return nextTab
        }

        fun newTab(): Array<FloatArray> {
            //generate random point
            val ran = Random()
            for (i in 0 until tab.size) {
                for (j in 0 until tab[i].size) {
                    this.tab[i][j] = ran.nextInt(2).toFloat()
                }
            }
            return this.tab
        }

        fun print(tab: Array<FloatArray>) {
            //print on terminal
            println()
            for (i in tab.indices) {
                for (j in 0 until tab[i].size) {
                    print(if (tab[i][j] == 0f) ". " else "# ")
                }
                println("")
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        //同步 lock object
        synchronized(this) {
            //divide step so we can get the numbers of row and column point on phone screen
            val xCount = width / step.toInt()
            val yCount = height / step.toInt()

            //First draw
            if (isFrist) {

                //initial points one dimension array, the total numbers of point is (X,Y)
                mPoints = FloatArray(xCount * yCount * 2)

                tab = lifeGame.newTab()
                lifeGame.print(tab)
                println()

                mPoints = FloatArray(xCount * yCount * 2)
                for (j in 0 until xCount )
                    for (i in 0 until yCount * 2) {
                        if (i/2 < rowSize && j < cellSize)
                            if (tab[i/2][j] != 0f) { //if not die
                                if (i % 2 == 0) {
                                    //x-axis
                                    mPoints[j * xCount * 2 + i] = i / 2 * step

                                } else {
                                    //y-axis
                                    mPoints[j * xCount * 2 + i] = j * step
                                }
                            }
                    }

                nextTab = lifeGame.nextTab(tab);
                isFrist = !isFrist
            }

            if (!Arrays.deepEquals(tab, nextTab)) {
                lifeGame.print(nextTab);
                mPoints = FloatArray(xCount * yCount * 2)
                for (j in 0 until xCount )
                    for (i in 0 until yCount * 2) {
                        if (i/2 < rowSize && j < cellSize)
                            if (tab[i/2][j] != 0f) { //if not die
                                if (i % 2 == 0) {
                                    //x-axis
                                    mPoints[j * xCount * 2 + i] = i / 2 * step

                                } else {
                                    //y-axis
                                    mPoints[j * xCount * 2 + i] = j * step
                                }
                            }

                    }
                System.out.println();
                tab = nextTab;
                nextTab = lifeGame.nextTab(tab);
            }

            //Random change color
            //paint.setARGB((0..255).random(),(0..255).random(), (0..255).random(), (0..255).random())

            canvas.drawPoints(mPoints, paint)

            //Delay one second to renew screen
            postInvalidateDelayed(1000)
        }
    }
}
