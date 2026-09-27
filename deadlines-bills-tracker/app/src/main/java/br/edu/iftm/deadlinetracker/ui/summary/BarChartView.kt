package br.edu.iftm.deadlinetracker.ui.summary

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import br.edu.iftm.deadlinetracker.R

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var bars: List<Bar> = emptyList()

    private val barsHeight = dp(130f)
    private val labelGap = dp(10f)
    private val labelHeight = dp(12f)
    private val barSpacing = dp(8f)
    private val maxBarWidth = dp(28f)
    private val minBarHeight = dp(4f)
    private val cornerRadius = dp(6f)
    private val rect = RectF()

    private val normalColor = ContextCompat.getColor(context, R.color.surface)
    private val highlightColor = ContextCompat.getColor(context, R.color.text)
    private val labelColor = ContextCompat.getColor(context, R.color.text_3)

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = ResourcesCompat.getFont(context, R.font.ibm_plex_mono_regular)
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9f, resources.displayMetrics)
        letterSpacing = 0.055f
        textAlign = Paint.Align.CENTER
    }

    /**
     * Recebe as barras calculadas pelo ViewModel e redesenha o gráfico.
     *
     * @param newBars barras na ordem de exibição.
     */
    fun setBars(newBars: List<Bar>) {
        bars = newBars
        invalidate()
    }

    /**
     * Usa toda a largura disponível e uma altura fixa igual à do frame Desempenho.
     *
     * @param widthMeasureSpec restrição de largura do pai.
     * @param heightMeasureSpec restrição de altura do pai.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = (barsHeight + labelGap + labelHeight).toInt()
        setMeasuredDimension(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    /**
     * Desenha as barras alinhadas pela base, proporcionais ao maior valor, com o rótulo embaixo.
     * Sem valores todas ficam com a altura mínima, como a barra de domingo no Figma.
     *
     * @param canvas superfície de desenho da view.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bars.isEmpty()) {
            return
        }
        val available = width - paddingLeft - paddingRight
        val gaps = barSpacing * (bars.size - 1)
        val column = (available - gaps) / bars.size
        val barWidth = minOf(maxBarWidth, column)
        val max = bars.maxOf { it.value }.coerceAtLeast(1L)
        val base = paddingTop + barsHeight
        val baseline = base + labelGap + labelHeight - textPaint.descent()

        bars.forEachIndexed { index, bar ->
            val center = paddingLeft + index * (column + barSpacing) + column / 2
            val barHeight = maxOf(minBarHeight, barsHeight * bar.value / max)
            rect.set(center - barWidth / 2, base - barHeight, center + barWidth / 2, base)
            barPaint.color = if (bar.highlighted) highlightColor else normalColor
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, barPaint)
            textPaint.color = if (bar.highlighted) highlightColor else labelColor
            canvas.drawText(bar.label, center, baseline, textPaint)
        }
    }

    /**
     * Converte dp em pixels.
     *
     * @param value medida em dp.
     * @return medida em pixels.
     */
    private fun dp(value: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)
}
