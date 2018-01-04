name := baseDirectory.value.getName

organization := "hobby.chenai.nakam"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.7"

//libraryProject := true

exportJars := true

// 可以去掉与 Scala 版本的关联
//crossPaths := false

// 这句在这里会导致编译报错 scala.reflect.internal.MissingRequirementError: object scala in compiler mirror not found.
//autoScalaLibrary := false

//proguardVersion := "5.2.1" // 必须高于 5.1，见 https://github.com/scala-android/sbt-android。

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies ++= Seq(
  "com.github.dedge-space" % "Annoguard" % "1.0.3-beta"
)
