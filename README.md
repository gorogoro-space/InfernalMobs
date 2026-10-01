# InfernalMobs
[![Paper 1.21.11](https://img.shields.io/badge/Paper-1.21.11-brightgreen.svg)](https://fill-ui.papermc.io/projects/paper/version/1.21.11)
[![GitHub release](https://img.shields.io/github/release/gorogoro-space/InfernalMobs.svg)](https://github.com/gorogoro-space/InfernalMobs/releases)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/gorogoro-space/InfernalMobs/issues)
[![License: CC BY-NC-SA 2.5 CA](https://img.shields.io/badge/License-CC%20BY--NC--SA%202.5%20CA-blue.svg)](https://creativecommons.org/licenses/by-nc-sa/2.5/ca/)

## 動作環境

- Paper 1.21.11 以降。Spigot と 1.21.11 未満のサーバーには対応していません(読み込まれません)
- Java 21 必須(サーバーの実行とビルドの両方。ビルドには JDK 21 が必要です。Gradle は同梱の Gradle Wrapper(9.8.0)を使うので、インストールは不要です)

## ビルド

本プロジェクトはビルドツールに Gradle を使用しています。

### コマンドラインでのビルド

```
gradlew clean build
```

### 🛠 IntelliJ IDEA でのビルド

1. IntelliJ IDEA の画面右端にある **「Gradle」タブ** をクリックして開きます。
2. プロジェクト名(InfernalMobs)を展開し、 **`Tasks`** ツリーを開きます。
3. リスト内にある **`clean`** をダブルクリックして実行します(古いビルドキャッシュを削除します)。
4. 続けてリスト内にある **`build`** をダブルクリックして実行します。

IntelliJ IDEA の「アーティファクトのビルド」はクラスファイルが入らないことがあるので使わないでください。

### 📦 生成されたファイルの場所

ビルドが成功すると、プロジェクトのルート直下に `build/libs` フォルダが作成(または更新)され、その中に JAR ファイルが生成されます。

* **生成先:** `build/libs/InfernalMobs-<version>.jar`(例: `InfernalMobs-6.9.3.jar`。バージョンは `build.gradle` の `version`)

この JAR ファイルを Minecraft サーバーの `plugins` フォルダに配置してください。

## 1.21 未満の設定ファイルからの移行

1.21 未満のサーバーで使っていた `config.yml` / `loot.yml` は、そのまま `plugins/InfernalMobs/` に置いて起動すれば、1.21 以降では使えない古い名前(MOB 名、効果名、ポーションの種類、アイテム名、エンチャント名)を自動で新しい名前に置き換えます。

- 置き換える前のファイルは `config.yml.pre1.21.bak` / `loot.yml.pre1.21.bak` として残ります
- 置き換えた内容はサーバーのログに出ます
- 自動で置き換えられない名前があった場合は、ログに警告が出ます。その箇所は手で直してください
- 以前のバージョンとは違い、サーバーのバージョンが変わっても `config.yml` は初期化されません

## 開発(IntelliJ IDEA で Claude Code を使う)

このリポジトリには、AI コーディングツール [Claude Code](https://docs.claude.com/ja/docs/claude-code/overview) 向けの作業ルールを書いた `CLAUDE.md` があります。IntelliJ IDEA で使う手順は次のとおりです。

1. Claude の有料プラン(Pro / Max)か、Anthropic API のアカウントを用意します。
2. Windows では、先に [Git for Windows](https://git-scm.com/downloads/win) をインストールします。
3. Claude Code 本体をインストールします。Windows では PowerShell で次を実行します。
   ```
   irm https://claude.ai/install.ps1 | iex
   ```
4. IntelliJ IDEA の「設定 → プラグイン → Marketplace」で「Claude Code」を検索してインストールし、IDE を再起動します。検索結果には Anthropic 以外が作った似た名前のプラグインも表示されます。プラグイン名の下に書かれた提供元が「Anthropic」になっているものを選んでください。
5. プロジェクトを開いて、ターミナルで `claude` を実行します(`Ctrl+Esc` でも起動できます)。初回はブラウザでログインします。

起動すると `CLAUDE.md` が自動で読み込まれ、このプロジェクトの設計方針に沿って作業します。

## kubotan へのメモ

リポジトリを新規作成(フォークを含む)したら、**必ず Watch を設定すること**(忘れない!)。
GitHub の自動 Watch 機能は 2025 年 5 月に廃止されたため、設定しないと他の人が立てた issue や PR の通知が届かない。

1. リポジトリのページ右上の **「Watch」** を押す
2. **「Custom」** を選び、**Issues** と **Pull requests** にチェックを入れる


# THIS IS FORK. #
Original source code is [here](https://bitbucket.org/Eliminator/infernalmobs/src/master/) by [Elim1nator](https://github.com/Elim1nator).

# README #

This is the Infernal Mobs source code. 

Released under the License: (CC BY-NC-SA 2.5 CA)
https://creativecommons.org/licenses/by-nc-sa/2.5/ca/

You can use it for private purposes or for non-commercial purposes if you give the author credit.
You MUST also link your changed source.

Thank you.
