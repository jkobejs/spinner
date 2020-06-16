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

/** Compiler plugin for working with partially applied types:
  * [[https://github.com/typelevel/kind-projector]]
  */
val KindProjectorVersion = "0.11.0"

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
val ZioVersion = "1.0.0-RC20"

/** Fast to write, Fast running Parsers in Scala
  * [[https://github.com/lihaoyi/fastparse]]
  */
val FastParseVersion = "2.2.2"

/** Fansi is a Scala library to make it easy to deal with fancy colored Ansi strings within your command-line programs.
  * [[https://github.com/lihaoyi/fansi]]
  */
val FansiVersion = "0.2.7"

/** Contextual is a small Scala library for defining your own string interpolators—prefixed string literals like url”https://propensive.com/”
  * [[https://github.com/propensive/contextual]]
  */
val ContextualVersion = "1.2.1"

/** The purely functional runtime system for Scala
  * [[https://github.com/typelevel/cats-effect]]
  */
val CatsEffectVersion = "2.1.3"

/**
  * Defines common plugins between all projects.
  */
def defaultPlugins: Project ⇒ Project = pr => {
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
  // scalaVersion               := "2.13.1",
  scalaVersion       := "2.12.10",
  crossScalaVersions := Seq("2.12.10", "2.13.1"),
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
  scalacOptions += "-P:silencer:pathFilters=.*[/]src_managed[/].*",
  addCompilerPlugin("org.typelevel"                      % "kind-projector" % KindProjectorVersion cross CrossVersion.full),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % BetterMonadicForVersion),
  addCompilerPlugin("com.github.ghik"                    % "silencer-plugin" % SilencerVersion cross CrossVersion.full),
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
      email = "jkobejs.dev@gmail.com",
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
    zioJVM,
    coreJVM,
    catsEffectJS,
    catsEffectJVM,
    zioDownloadExampleJVM,
    zioDownloadExampleJS,
    catsEffectDownloadExampleJVM,
    catsEffectDownloadExampleJS,
    zioFinebarsExampleJVM,
    zioFinebarsExampleJS
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
      micrositeDescription      := "I like when it Spinns!",
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
      "com.lihaoyi" %%% "fastparse"     % FastParseVersion,
      "dev.zio" %% "zio"                % ZioVersion,
      "com.propensive" %%% "contextual" % ContextualVersion,
      "com.lihaoyi" %%% "fansi"         % FansiVersion,
      "io.estatico" %%% "newtype"       % NewtypeVersion % Provided,
      "org.typelevel" %%% "simulacrum"  % SimulacrumVersion % Provided,
      // For testing
      "dev.zio" %% "zio-test"     % ZioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % ZioVersion % Test
    )
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val zio = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("zio"))
  .configureCross(defaultCrossProjectConfiguration)
  .settings(
    name := "spinner-zio",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % ZioVersion,
      // For testing
      "dev.zio" %% "zio-test"     % ZioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % ZioVersion % Test
    )
  )
  .dependsOn(core)

lazy val zioJVM = zio.jvm
lazy val zioJS = zio.js

lazy val catsEffect = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("cats-effect"))
  .configureCross(defaultCrossProjectConfiguration)
  .settings(
    name := "spinner-cats-effect",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % CatsEffectVersion
    )
  )
  .dependsOn(core)

lazy val catsEffectJVM = catsEffect.jvm
lazy val catsEffectJS = catsEffect.js

// examples

// lazy val examples = crossProject(JSPlatform, JVMPlatform)
//   .crossType(CrossType.Full)
//   .in(file("examples"))
//   .configureCross(defaultCrossProjectConfiguration)
//   .settings(
//     name := "examples"
//   )
//   .settings(doNotPublishArtifact)
//   .dependsOn(zio, catsEffect)

// lazy val examplesJVM = examples.jvm
// lazy val examplesJS = examples.js

// download example

lazy val zioDownloadExample = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("examples/zio/download"))
  .configureCross(defaultCrossProjectConfiguration)
  .settings(
    name := "zio-download-example",
    graalVMNativeImageOptions ++= Seq(
      "--initialize-at-build-time",
      "--no-fallback"
    )
  )
  .settings(doNotPublishArtifact)
  .dependsOn(zio)
  .enablePlugins(GraalVMNativeImagePlugin)

lazy val zioDownloadExampleJVM = zioDownloadExample.jvm
lazy val zioDownloadExampleJS = zioDownloadExample.js

lazy val catsEffectDownloadExample = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("examples/cats-effect/download"))
  .configureCross(defaultCrossProjectConfiguration)
  .settings(
    name                            := "cats-effect-download-example",
    scalaJSUseMainModuleInitializer := true
  )
  .settings(doNotPublishArtifact)
  .dependsOn(catsEffect)

lazy val catsEffectDownloadExampleJVM = catsEffectDownloadExample.jvm
lazy val catsEffectDownloadExampleJS = catsEffectDownloadExample.js

// finebars example
lazy val zioFinebarsExample = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("examples/zio/finebars"))
  .configureCross(defaultCrossProjectConfiguration)
  .settings(
    name := "zio-finebars-example",
    graalVMNativeImageOptions ++= Seq(
      "--initialize-at-build-time",
      "--no-fallback"
    )
  )
  .settings(doNotPublishArtifact)
  .dependsOn(zio)
  .enablePlugins(GraalVMNativeImagePlugin)

lazy val zioFinebarsExampleJVM = zioFinebarsExample.jvm
lazy val zioFinebarsExampleJS = zioFinebarsExample.js

// Reloads build.sbt changes whenever detected
Global / onChangedBuildSource := ReloadOnSourceChanges
