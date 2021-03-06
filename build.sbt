import BuildKeys._
import Boilerplate._

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import sbtcrossproject.CrossProject

// ---------------------------------------------------------------------------
// Commands

addCommandAlias("release", ";+clean ;ci-release ;unidoc ;microsite/publishMicrosite")
addCommandAlias("ci", ";project root ;reload ;+clean ;+test:compile ;+test ;+package ;unidoc ;site/makeMicrosite")
addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
// ---------------------------------------------------------------------------
// Dependencies

/** Newtype (opaque type) definitions:
  * [[https://github.com/estatico/scala-newtype]]
  */
val NewtypeVersion = "0.4.3"

/** First-class support for type-classes:
  * [[https://github.com/typelevel/simulacrum]]
  */
val SimulacrumVersion = "1.0.0"

/** For macros that are supported on older Scala versions.
  * Not needed starting with Scala 2.13.
  */
val MacroParadiseVersion = "2.1.0"

/** Library for property-based testing:
  * [[https://www.scalacheck.org/]]
  */
val ScalaCheckVersion = "1.14.3"

/** Compiler plugin for fixing "for comprehensions" to do desugaring w/o `withFilter`:
  * [[https://github.com/typelevel/kind-projector]]
  */
val BetterMonadicForVersion = "0.3.1"

/** Compiler plugin for silencing compiler warnings:
  * [[https://github.com/ghik/silencer]]
  */
val SilencerVersion = "1.6.0"

/** A type-safe, composable library for async and concurrent programming in Scala
  * [[https://github.com/zio/zio]]
  */
val ZioVersion = "1.0.0-RC21"

/** Fast to write, Fast running Parsers in Scala
  * [[https://github.com/lihaoyi/fastparse]]
  */
val FastParseVersion = "2.3.0"

/** Fansi is a Scala library to make it easy to deal with fancy colored Ansi strings within your command-line programs.
  * [[https://github.com/lihaoyi/fansi]]
  */
val FansiVersion = "0.2.9"

/** Contextual is a small Scala library for defining your own string interpolators???prefixed string literals like url???https://propensive.com/???
  * [[https://github.com/propensive/contextual]]
  */
val ContextualVersion = "3.0.0"

/**
  * Defines common plugins between all projects.
  */
def defaultPlugins: Project ??? Project = pr => {
  val withCoverage = sys.env.getOrElse("SBT_PROFILE", "") match {
    case "coverage" => pr
    case _ => pr.disablePlugins(scoverage.ScoverageSbtPlugin)
  }
  withCoverage
    .enablePlugins(AutomateHeaderPlugin)
    .enablePlugins(GitBranchPrompt)
}

lazy val sharedSettings = Seq(
  projectTitle               := "spinner",
  projectWebsiteRootURL      := "https://jkobejs.github.io/",
  projectWebsiteBasePath     := "/spinner/",
  githubOwnerID              := "jkobejs",
  githubRelativeRepositoryID := "spinner",
  organization               := "io.github.jkobejs",
  scalaVersion               := "2.13.2",
  crossScalaVersions         := Seq("2.12.11", "2.13.2"),
  // More version specific compiler options
  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v <= 12 =>
      Seq(
        "-Ypartial-unification"
      )
    case _ =>
      Seq(
        // Replaces macro-paradise in Scala 2.13
        "-Ymacro-annotations"
      )
  }),
  // Turning off fatal warnings for doc generation
  scalacOptions.in(Compile, doc) ~= filterConsoleScalacOptions,
  // Silence all warnings from src_managed files
  // scalacOptions += "-P:silencer:pathFilters=.*[/]src_managed[/].*",
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % BetterMonadicForVersion),
  // addCompilerPlugin("com.github.ghik"                    % "silencer-plugin" % SilencerVersion cross CrossVersion.full),
  // ScalaDoc settings
  autoAPIMappings := true,
  scalacOptions in ThisBuild ++= Seq(
    // Note, this is used by the doc-source-url feature to determine the
    // relative path of a given source file. If it's not a prefix of a the
    // absolute path of the source file, the absolute path of that file
    // will be put into the FILE_SOURCE variable, which is
    // definitely not what we want.
    "-sourcepath",
    file(".").getAbsolutePath.replaceAll("[.]$", "")
  ),
  // https://github.com/sbt/sbt/issues/2654
  incOptions := incOptions.value.withLogRecompileOnMacro(false),
  // ---------------------------------------------------------------------------
  // Options for testing
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  logBuffered in Test            := false,
  logBuffered in IntegrationTest := false,
  // Disables parallel execution
  parallelExecution in Test             := false,
  parallelExecution in IntegrationTest  := false,
  testForkedParallel in Test            := false,
  testForkedParallel in IntegrationTest := false,
  concurrentRestrictions in Global += Tags.limit(Tags.Test, 1),
  // ---------------------------------------------------------------------------
  // Options meant for publishing on Maven Central
  publishArtifact in Test := false,
  pomIncludeRepository := { _ =>
    false
  }, // removes optional dependencies
  licenses      := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  homepage      := Some(url(projectWebsiteFullURL.value)),
  headerLicense := Some(HeaderLicense.Custom(s"""|Copyright (c) 2020 the ${projectTitle.value} contributors.
                                                 |See the project homepage at: ${projectWebsiteFullURL.value}
                                                 |
                                                 |Licensed under the Apache License, Version 2.0 (the "License");
                                                 |you may not use this file except in compliance with the License.
                                                 |You may obtain a copy of the License at
                                                 |
                                                 |    http://www.apache.org/licenses/LICENSE-2.0
                                                 |
                                                 |Unless required by applicable law or agreed to in writing, software
                                                 |distributed under the License is distributed on an "AS IS" BASIS,
                                                 |WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                                                 |See the License for the specific language governing permissions and
                                                 |limitations under the License.""".stripMargin)),
  scmInfo := Some(
    ScmInfo(
      url(s"https://github.com/${githubFullRepositoryID.value}"),
      s"scm:git@github.com:${githubFullRepositoryID.value}.git"
    )),
  developers := List(
    Developer(
      id = "jkobejs",
      name = "Josip Grgurica",
      email = "josip.grgurica@protonmail.com",
      url = url("https://jkobejs.github.io")
    )),
  // -- Settings meant for deployment on oss.sonatype.org
  sonatypeProfileName := organization.value
)

/**
  * Shared configuration across all sub-projects with actual code to be published.
  */
def defaultCrossProjectConfiguration(pr: CrossProject) = {
  val sharedJavascriptSettings = Seq(
    coverageExcludedFiles := ".*",
    // Use globally accessible (rather than local) source paths in JS source maps
    scalacOptions += {
      val tagOrHash = {
        val ver = s"v${version.value}"
        if (isSnapshot.value)
          git.gitHeadCommit.value.getOrElse(ver)
        else
          ver
      }
      val l = (baseDirectory in LocalRootProject).value.toURI.toString
      val g = s"https://raw.githubusercontent.com/${githubFullRepositoryID.value}/$tagOrHash/"
      s"-P:scalajs:mapSourceURI:$l->$g"
    }
  )

  val sharedJVMSettings = Seq(
    skip.in(publish) := customScalaJSVersion.isDefined
  )

  pr.configure(defaultPlugins)
    .settings(sharedSettings)
    .jsSettings(sharedJavascriptSettings)
    .jvmSettings(doctestTestSettings(DoctestTestFramework.Minitest))
    .jvmSettings(sharedJVMSettings)
    .settings(crossVersionSharedSources)
    .settings(requiredMacroCompatDeps(MacroParadiseVersion))
    .settings(filterOutMultipleDependenciesFromGeneratedPomXml(
      "groupId" -> "org.scoverage".r :: Nil,
      "groupId" -> "io.estatico".r :: "artifactId" -> "newtype".r :: Nil,
      "groupId" -> "org.typelevel".r :: "artifactId" -> "simulacrum".r :: Nil
    ))
}

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaUnidocPlugin)
  .aggregate(
    coreJVM,
    coreJS,
    zioDownloadExampleJVM,
    zioDownloadExampleJS,
    zioFinebarsExampleJVM,
    zioFinebarsExampleJS,
    zioSingleExampleJVM,
    zioSingleExampleJS,
    zioLogExampleJVM,
    zioLogExampleJS,
    zioLongSpinnerExampleJVM,
    zioLongSpinnerExampleJS
  )
  .configure(defaultPlugins)
  .settings(sharedSettings)
  .settings(doNotPublishArtifact)
  .settings(unidocSettings(coreJVM))
  .settings(
    // Try really hard to not execute tasks in parallel ffs
    Global / concurrentRestrictions := Tags.limitAll(1) :: Nil
  )

lazy val site = project
  .in(file("site"))
  .disablePlugins(MimaPlugin)
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(MdocPlugin)
  .settings(sharedSettings)
  .settings(doNotPublishArtifact)
  .dependsOn(coreJVM)
  .settings {
    import microsites._
    Seq(
      micrositeName             := projectTitle.value,
      micrositeDescription      := "You spin me right round",
      micrositeAuthor           := "Josip Grgurica",
      micrositeTwitterCreator   := "@jkobejs",
      micrositeGithubOwner      := githubOwnerID.value,
      micrositeGithubRepo       := githubRelativeRepositoryID.value,
      micrositeUrl              := projectWebsiteRootURL.value.replaceAll("[/]+$", ""),
      micrositeBaseUrl          := projectWebsiteBasePath.value.replaceAll("[/]+$", ""),
      micrositeDocumentationUrl := s"${projectWebsiteFullURL.value.replaceAll("[/]+$", "")}/${docsMappingsAPIDir.value}/",
      micrositeGitterChannelUrl := githubFullRepositoryID.value,
      micrositeFooterText       := None,
      micrositeHighlightTheme   := "atom-one-light",
      micrositePalette := Map(
        "brand-primary" -> "#3e5b95",
        "brand-secondary" -> "#294066",
        "brand-tertiary" -> "#2d5799",
        "gray-dark" -> "#49494B",
        "gray" -> "#7B7B7E",
        "gray-light" -> "#E5E5E6",
        "gray-lighter" -> "#F4F3F4",
        "white-color" -> "#FFFFFF"
      ),
      micrositeCompilingDocsTool                       := WithMdoc,
      fork in mdoc                                     := true,
      scalacOptions.in(Tut)                            ~= filterConsoleScalacOptions,
      libraryDependencies += "com.47deg" %% "github4s" % "0.23.0",
      micrositePushSiteWith                            := GitHub4s,
      micrositeGithubToken                             := sys.env.get("GITHUB_TOKEN"),
      micrositeExtraMdFiles := Map(
        file("CODE_OF_CONDUCT.md") -> ExtraMdFileConfig(
          "CODE_OF_CONDUCT.md",
          "page",
          Map("title" -> "Code of Conduct", "section" -> "code of conduct", "position" -> "100")),
        file("LICENSE.md") -> ExtraMdFileConfig(
          "LICENSE.md",
          "page",
          Map("title" -> "License", "section" -> "license", "position" -> "101"))
      ),
      docsMappingsAPIDir := s"api",
      addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc) in root, docsMappingsAPIDir),
      sourceDirectory in Compile := baseDirectory.value / "src",
      sourceDirectory in Test    := baseDirectory.value / "test",
      mdocIn                     := (sourceDirectory in Compile).value / "mdoc",
      // Bug in sbt-microsites
      micrositeConfigYaml := microsites.ConfigYml(
        yamlCustomProperties = Map("exclude" -> List.empty[String])
      )
    )
  }

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("core"))
  .configureCross(defaultCrossProjectConfiguration)
  .settings(
    name := "spinner-core",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "fastparse" % FastParseVersion,
      "com.lihaoyi" %%% "fansi"     % FansiVersion,
      "dev.zio" %%% "zio"           % ZioVersion,
      // "com.propensive" %%% "contextual" % ContextualVersion,
      // For testing
      "dev.zio" %%% "zio-test"     % ZioVersion % Test,
      "dev.zio" %%% "zio-test-sbt" % ZioVersion % Test
    )
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val zioDownloadExample = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("examples/zio/download"))
  .configureCross(defaultCrossProjectConfiguration)
  .settings(
    name                            := "zio-download-example",
    scalaJSUseMainModuleInitializer := true,
    graalVMNativeImageOptions ++= Seq(
      "--initialize-at-build-time",
      "--no-fallback"
    ),
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.0.0"
    )
  )
  .settings(doNotPublishArtifact)
  .dependsOn(core)
  .enablePlugins(GraalVMNativeImagePlugin)

lazy val zioDownloadExampleJVM = zioDownloadExample.jvm
lazy val zioDownloadExampleJS = zioDownloadExample.js

// finebars example
lazy val zioFinebarsExample = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("examples/zio/finebars"))
  .configureCross(defaultCrossProjectConfiguration)
  .settings(
    name                            := "zio-finebars-example",
    scalaJSUseMainModuleInitializer := true,
    graalVMNativeImageOptions ++= Seq(
      "--initialize-at-build-time",
      "--no-fallback"
    ),
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.0.0"
    )
  )
  .settings(doNotPublishArtifact)
  .dependsOn(core)
  .enablePlugins(GraalVMNativeImagePlugin)

lazy val zioFinebarsExampleJVM = zioFinebarsExample.jvm
lazy val zioFinebarsExampleJS = zioFinebarsExample.js

// single example
lazy val zioSingleExample = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("examples/zio/single"))
  .configureCross(defaultCrossProjectConfiguration)
  .settings(
    name                            := "zio-single-example",
    scalaJSUseMainModuleInitializer := true,
    graalVMNativeImageOptions ++= Seq(
      "--initialize-at-build-time",
      "--no-fallback"
    ),
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.0.0"
    )
  )
  .settings(doNotPublishArtifact)
  .dependsOn(core)
  .enablePlugins(GraalVMNativeImagePlugin)

lazy val zioSingleExampleJVM = zioSingleExample.jvm
lazy val zioSingleExampleJS = zioSingleExample.js

// log example
lazy val zioLogExample = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("examples/zio/log"))
  .configureCross(defaultCrossProjectConfiguration)
  .settings(
    name                            := "zio-log-example",
    scalaJSUseMainModuleInitializer := true,
    graalVMNativeImageOptions ++= Seq(
      "--initialize-at-build-time",
      "--no-fallback"
    ),
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.0.0"
    )
  )
  .settings(doNotPublishArtifact)
  .dependsOn(core)
  .enablePlugins(GraalVMNativeImagePlugin)

lazy val zioLogExampleJVM = zioLogExample.jvm
lazy val zioLogExampleJS = zioLogExample.js

// long spinner example
lazy val zioLongSpinnerExample = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("examples/zio/long-spinner"))
  .configureCross(defaultCrossProjectConfiguration)
  .settings(
    name                            := "zio-log-example",
    scalaJSUseMainModuleInitializer := true,
    graalVMNativeImageOptions ++= Seq(
      "--initialize-at-build-time",
      "--no-fallback"
    ),
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.0.0"
    )
  )
  .settings(doNotPublishArtifact)
  .dependsOn(core)
  .enablePlugins(GraalVMNativeImagePlugin)

lazy val zioLongSpinnerExampleJVM = zioLongSpinnerExample.jvm
lazy val zioLongSpinnerExampleJS = zioLongSpinnerExample.js

// Reloads build.sbt changes whenever detected
Global / onChangedBuildSource := ReloadOnSourceChanges
