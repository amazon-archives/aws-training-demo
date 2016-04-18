/*
Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

    http://aws.amazon.com/apache2.0/

or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
*/

package aws.daleks.util

import java.util.concurrent.atomic.AtomicBoolean

sealed trait Humid[+A] extends Traversable[A] {
  def flatMap[B](f: A => Humid[B]): Humid[B]
  def get:A
  def isEmpty:Boolean

  override def foreach[U](f: A => U): Unit =
      if (! isEmpty) f(get)
}

case class Wet[+A](a: A) extends Humid[A] {
  override def flatMap[B](f: A => Humid[B]): Humid[B] = f(a)
  override def get:A = a
  override def isEmpty:Boolean = false
}

case object Dry extends Humid[Nothing] {
  override def flatMap[B](f: Nothing => Humid[B]): Humid[B] = Dry
  override def isEmpty = true
  override def get = throw new NoSuchElementException("Dry.get")
}

object Humid {
  val globalDry = new AtomicBoolean(true)
  def isDry = globalDry.get
  def apply[A](x:A):Humid[A] = if (isDry) Dry else Wet(x)
}

object HumidTest extends App {
  Humid("Uala !!! 1").foreach(println(_));
  Humid.globalDry.set(false)
  Humid("Uala !!! 2").foreach(println(_));
  val t = Humid("Something").map {i => i.toUpperCase() + " uala" }
  t.foreach(println(_))


}
