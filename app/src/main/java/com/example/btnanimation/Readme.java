package com.example.btnanimation;

/**
 * Created by anchaoguang on 2019-11-25.
 * 1.实现view的三个构造器
 * 2.其中的参数分别为：Context：上下文、AttributeSet attrs： 从xml中定义的参数、int defStyleAttr ：主题中优先级最高的属性、int defStyleRes  ： 优先级次之的内置于View的style
 * 其中优先级依次为： Xml直接定义 > xml中style引用 > defStyleAttr > defStyleRes > theme直接定义
 * 3.在第三个构造器中，初始化画笔（图形画笔， 文字画笔， 打钩画笔），对动画结束进行监听
 * 4.重写 onSizeChanged方法， w,h为控件宽高，并且初始化ok路径动画的路径，再初始化所有动画
 * 5.分别定义矩形变圆角矩形、圆角矩形变圆、向上移动view、圆中打钩动画方法
 * 6.将每种动画都放入统一的animatorSet中
 * 7.重写onDraw()方法，定义长方形变为圆形的方法（参数从【矩形变为圆角矩形】【圆角矩形变圆】取）
 * 定义写文字的方法，使用参数确定是否执行绘制打钩的动画（如果不用参数确定，一开始就会有勾）
 * 8.定义开始动画的方法，从activity中调用，定义结束时重置的方法，在监听到动画结束时调用
 */

