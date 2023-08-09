package com.atguigu.scala.chapter06

import scala.beans.BeanProperty

/**
  * Scala - 面向对象编程 - 属性
  */
object Scala06_field {
  def main(args: Array[String]): Unit = {
    /*
      Java的属性:
        private String name ;   // 默认初始化
        public void setName(String name ){ this.name = name }
        public String getName(){return this.name}



      Scala的属性:
        [修饰符] var name : String = _

        1. Scala中声明好属性后，会自动提供两个方法:
           例如: 声明name属性， 提供的两个方法如下：
                 name()      -> "get"
                 name_$eq()  -> "set"

        2.不同的访问权限修饰符修饰属性:
          private     => 属性私有,方法也私有
          private[包] => 属性私有,方法public
          protected   => 属性私有,方法public
          default     => 属性私有,方法public


        3. var  和  val 声明属性:
          var声明属性表示可以改, 会提供 "get"/"set"

          val 生命属性表示不可以改,只提供" get"


        4. get / set
           反射操作属性对应的get/set方法
           反射操作属性

           如果希望提供真正的get/set方法， 只需要在属性上加@BeanProperty注解即可.

     */


  }
}

class User06{

  private var name : String  = _

  private[chapter06] var age : Int = _

  protected var money : Int = _

  @BeanProperty
  var address : String = _

  val sex : String = "男"
}
