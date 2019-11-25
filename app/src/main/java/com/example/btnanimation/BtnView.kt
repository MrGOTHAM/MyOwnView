package com.example.btnanimation

import android.animation.*
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast

/**
 * Created by anchaoguang on 2019-11-23.
 * Animation 不能响应交互
 * Animator 可以响应交互
 * 说明文档   https://www.jianshu.com/p/3eb9777f6ab7
 */
class BtnView : View{

    private val mPaint: Paint by lazy { Paint() }
    private val mTextPaint: Paint by lazy { Paint() }
    private val mOkPaint: Paint by lazy { Paint() }
    private val mPath: Path by lazy { Path() }
    private val mRect: Rect by lazy { Rect() }
    private val mRectF: RectF by lazy { RectF() }
    private val mTextRect: Rect by lazy { Rect() }
    private lateinit var mPathMeasure: PathMeasure
    private var mViewWidth = 0
    private var mViewHeight = 0
    private var mDefTwoCircleDistance = 0
    private var mCircleAngle: Int = 0
    private var mTwoCircleDistance = 0

    private val mAnimatorSet: AnimatorSet by lazy { AnimatorSet() }
    private lateinit var mAnimatorRectToAngle: ValueAnimator
    private lateinit var mAnimatorRectToCircle: ValueAnimator
    private lateinit var mAnimatorMoveToUp: ObjectAnimator
    private lateinit var mAnimatorDrawOk: ValueAnimator
    private var startDrawPath: Boolean = false

    constructor(context: Context) : super(context, null)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        initPaint()
        // AnimatorListenerAdapter 可以只实现部分接口
    }

    private fun initPaint() {
        mPaint.strokeWidth = 4f
        mPaint.style = Paint.Style.FILL
        mPaint.isAntiAlias = true
        mPaint.color = 0xffbc7d53.toInt()

        mTextPaint.isAntiAlias = true
        mTextPaint.textSize = 40f
        mTextPaint.color = Color.WHITE
        // 字符串水平居中
        mTextPaint.textAlign = Paint.Align.CENTER

        mOkPaint.strokeWidth = 10f
        mOkPaint.style = Paint.Style.STROKE
        mOkPaint.isAntiAlias = true
        mOkPaint.color = Color.WHITE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initPaint()
        mViewWidth = w
        mViewHeight = h

        mDefTwoCircleDistance = (w - h) / 2
        initOk()
        initAnimation()
        initView()
    }

    private fun initOk() {
        mPath.moveTo(mDefTwoCircleDistance + height / 8f * 3, height / 2f)
        mPath.lineTo(mDefTwoCircleDistance + height / 2f, height / 5f * 3)
        mPath.lineTo(mDefTwoCircleDistance + height / 3f * 2, height / 5f * 2)
        // 第一个参数，被测量的路径， 第二个：如果为true，则将成为封闭式view
        mPathMeasure = PathMeasure(mPath, true)
    }
    private fun initView(){
        mAnimatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                reset()
            }
        })
    }

    private fun initAnimation() {
        setRectToAngleAnimation()
        setRectToCircleAnimation()
        setMoveToUpAnimation()
        setDrawOkAnimation()

        mAnimatorSet.play(mAnimatorMoveToUp)
            .before(mAnimatorDrawOk)
            .after(mAnimatorRectToCircle)
            .after(mAnimatorRectToAngle)
    }

    private fun setRectToAngleAnimation() {
        mAnimatorRectToAngle = ValueAnimator.ofInt(0, height / 2)
        mAnimatorRectToAngle.duration = 1000
        mAnimatorRectToAngle.addUpdateListener { animation ->
            mCircleAngle = animation.animatedValue as Int
            invalidate()
        }
    }

    private fun setRectToCircleAnimation() {
        mAnimatorRectToCircle = ValueAnimator.ofInt(0, mDefTwoCircleDistance)
        mAnimatorRectToCircle.duration = 1000
        mAnimatorRectToCircle.addUpdateListener { animation ->
            mTwoCircleDistance = animation.animatedValue as Int
            // 圆角变为圆的过程中，字逐渐消失， 透明度为0-255阶，255为不透明，mTwoCircleDistance 最后会变为 mDefTwoCircleDistance
            val alpha = 255 - (mTwoCircleDistance * 255/ mDefTwoCircleDistance )
            mTextPaint.alpha = alpha
            invalidate()
        }
    }

    private fun setMoveToUpAnimation() {
        val currentTranslateY = this.translationY
        mAnimatorMoveToUp = ObjectAnimator.ofFloat(this, "translationY", currentTranslateY, currentTranslateY - 300)
        mAnimatorMoveToUp.duration = 1000
        mAnimatorMoveToUp.interpolator = AccelerateDecelerateInterpolator()
    }

    private fun setDrawOkAnimation() {
        mAnimatorDrawOk = ValueAnimator.ofFloat(1f, 0f)
        mAnimatorDrawOk.duration = 1000
        mAnimatorDrawOk.addUpdateListener {animation ->
            // startDrawPath 必须放在监听器内，否则出问题
            startDrawPath = true
            val  value = animation.animatedValue as Float
            // 对路径动画实施绘制动画效果, 两段路径在一秒内完成
            val effect = DashPathEffect(floatArrayOf(mPathMeasure.length, mPathMeasure.length), value * mPathMeasure.length)
            Log.i("ancg", "value=====$value")
            Log.i("ancg", "effect====$effect")
            mOkPaint.pathEffect = effect
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawOvalToCircle(canvas)
        drawText(canvas)
        if (startDrawPath){
            canvas.drawPath(mPath, mOkPaint)
        }
    }

    private fun drawText(canvas: Canvas) {
        mTextRect.left = 0
        mTextRect.right = mViewWidth
        mTextRect.top = 0
        mTextRect.bottom = height
        // fontMetrics 必须在设置字体、样式之后调用，否则可能会出问题， fontMetrics 为字体间距
        val fontMetrics = mTextPaint.fontMetricsInt
        // fontMetrics.top 为基线到字体上边框的距离，fontMetrics.bottom 为基线到字体下边框的距离
        // baseLine = （总高度 - 基线到上边框距离 - 基线到下边框距离）/ 2
        val baseLineY = (mTextRect.bottom + mTextRect.top - fontMetrics.bottom - fontMetrics.top) / 2
        // 参数为：String， x坐标， 基线Y， 画笔
        canvas.drawText("确认完成", mTextRect.centerX().toFloat(), baseLineY.toFloat(), mTextPaint)
    }

    private fun drawOvalToCircle(canvas: Canvas) {
        mRectF.left = mTwoCircleDistance.toFloat()
        mRectF.top = 0f
        mRectF.bottom = height.toFloat()
        mRectF.right = (mViewWidth - mTwoCircleDistance).toFloat()
        canvas.drawRoundRect(mRectF, mCircleAngle.toFloat(), mCircleAngle.toFloat(), mPaint)
    }

    fun start(){
        mAnimatorSet.start()
    }

    fun reset(){
        if (translationY == -300f){
            startDrawPath = false
            mCircleAngle = 0
            mTwoCircleDistance = 0
            mDefTwoCircleDistance = (mViewWidth - mViewHeight)/2
            mTextPaint.alpha = 255
            translationY += 300f
            invalidate()
        } else {
            Toast.makeText(context, "数据异常",Toast.LENGTH_SHORT).show()
        }
    }
}
