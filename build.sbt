
name := baseDirectory.value.getName

organization := "hobby.chenai.nakam"

version := "1.0.0"

scalaVersion := "2.11.12"

crossScalaVersions := Seq(
  /*"2.11.7", 多余，不需要两个*/
  "2.11.12",
  /*"2.12.2", 有一些编译问题：`the interface is not a direct parent`。*/
  "2.12.12")

//libraryProject := true

exportJars := true

// 可以去掉与 Scala 版本的关联
//crossPaths := false

// 这句在这里会导致编译报错 scala.reflect.internal.MissingRequirementError: object scala in compiler mirror not found.
//autoScalaLibrary := false

//proguardVersion := "5.2.1" // 必须高于 5.1，见 https://github.com/scala-android/sbt-android。

offline := true

// 解决生成文档报错导致 jitpack.io 出错的问题。
publishArtifact in packageDoc := false

// 如果要用 jitpack 打包的话就加上，打完了再注掉。
resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies ++= Seq(
  "com.github.bdo-cash" % "annoguard" % "v1.0.5-beta",

  "junit" % "junit" % "[4.12,)" % Test,
  // `3.2.0-SNAP10`会导致`scala.ScalaReflectionException: object org.scalatest.prop.Configuration$ not found`.
  "org.scalatest" %% "scalatest" % "3.2.0-SNAP7" % Test
)

// 如果项目要独立编译，请同时启用这部分。
// Macro Settings
///*
resolvers += Resolver.sonatypeRepo("releases")
addCompilerPlugin("org.scalamacros" % "paradise" % "[2.1.0,)" cross CrossVersion.full)
// https://mvnrepository.com/artifact/org.scala-lang/scala-compiler
libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value
//*/

/*
// 以下已弃用：
resolvers += Resolver.sonatypeRepo("releases")
// scalameta/paradise currently supports scalameta 1.8.0 only, not 2.0.0-M1.
// (https://stackoverflow.com/questions/45470048/new-style-inline-macros-require-scala-meta)
libraryDependencies ++= Seq(
  "org.scalameta" %% "scalameta" % "1.8.0" /*[3.7.4,)*/ % Provided,
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
)
addCompilerPlugin("org.scalameta" % "paradise" % "[3.0.0-M9,)" cross CrossVersion.full)
//addCompilerPlugin("org.scalamacros" % "paradise" % "[2.1.0,)" cross CrossVersion.full)
scalacOptions += "-Xplugin-require:macroparadise"
scalacOptions in(Compile, console) ~= (_ filterNot (_ contains "paradise")) // macroparadise plugin doesn't work in repl yet.
scalacOptions in console in Compile -= "-Xfatal-warnings"
scalacOptions in console in Test -= "-Xfatal-warnings"
*/
