# InfernalMobs

ランダムな能力を持った強化 MOB(Infernal Mob)を出現させ、倒すと特別な戦利品を落とす Paper 用プラグイン(Paper 1.21.11 以降・Java 21 必須。Spigot や 1.21.11 未満での動作は考慮しない)。
リポジトリ: https://github.com/gorogoro-space/InfernalMobs (公開リポジトリ。Elim1nator/Infernal-Mobs のフォーク)
原作: [Elim1nator](https://github.com/Elim1nator) / 元のソースは https://bitbucket.org/Eliminator/infernalmobs/src/master/
ライセンス: CC BY-NC-SA 2.5 CA(非営利のみ、作者のクレジットを表示し、変更したソースへのリンクを示す必要がある)

## 作業の進め方(必ず守ること)

- **設計が確定するまで実装しない。** 機能追加や仕様変更は、まず設計案(何を・なぜ・どう変えるか、影響範囲)を提示し、承認を得てからコードを書く。
- 判断が必要な点は、選択肢を示して質問する。勝手に決めない。
- やり取りは日本語で行う。新しく書くコードのコメントは日本語でよい。ただし、原作の英語のコメントやプレイヤー向けメッセージは、頼まれない限り翻訳・書き換えをしない(原作との差分を小さく保つため)。
- 変更は必要最小限にする。頼まれていないリファクタリングや機能追加はしない。原作のコードは整形・リネームもしない。
- 作業後は、変更・追加・削除したファイルの一覧と変更内容を報告する。
- 仕様を変えたら README.md と CLAUDE.md も合わせて更新する。README.md の末尾にある原作の README(ライセンス表記)は消さない。
- `git push` の前には必ず確認を取る。コミットは意味のある単位で分ける。
- コミットメッセージや PR に `Co-Authored-By: Claude` などの署名を付けない。`.claude/` は Git に入れない(`.git/info/exclude` で除外済み)。
- このリポジトリはフォークなので、`gh pr create` の既定の送り先は原作のリポジトリになる。PR は必ず `--repo gorogoro-space/InfernalMobs --base main` を指定して自分のリポジトリに作る。原作へ PR を出すのは頼まれたときだけ。
- 実装中に設計の抜けや穴に気づいたら、黙って対処せず報告して相談する。
- プルリクエストをチェックするときは、次の観点で確認して報告する。
  - 変更概要(何を・なぜ変えているか)
  - 脆弱性(権限チェックの漏れ、入力値の検証、パスの扱いなど)
  - 安全面(データの破損・消失、他プラグインの妨げ、古いバージョンのサーバーでの互換性など)
  - 性能(TPS など)の低下(高頻度イベントでの重い処理、メインスレッドでの同期 I/O、定期タスクの追加など。「最重要の設計方針」に沿っているか)

## 環境

- Paper API 1.21.11(`io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT`、`compileOnly`)/ `plugin.yml` の `api-version: '1.21.11'`(1.21.11 未満のサーバーでは Paper が読み込まない)
- **Java 21 必須。** `build.gradle` の toolchain で常に JDK 21 を使ってビルドし、Java 21 の class ファイルを出力する。勝手に変えない
- ビルド: Gradle(Groovy DSL、wrapper 9.8.0)。`gradlew clean build`。「ビルドして」と言われたら常にクリーンビルドする。成果物は `build/libs/InfernalMobs-<version>.jar`
- バージョンは `build.gradle` の `version` だけで管理する。`plugin.yml` の `version: '${version}'` はビルド時に置き換わる
- パッケージ: `io.hotmail.com.jacob_vejvoda.infernal_mobs`、メインクラス: `infernal_mobs`(小文字始まりだが原作のまま)。**パッケージ名・クラス名は変えない。** plugin.yml の `main` と一致しなくなると起動せず、`InfernalSpawnEvent` などを使う他のプラグインも動かなくなる
- ソースは UTF-8。色コードの `§` などを含むので、ほかの文字コードで保存しない(文字化けの修正履歴あり)
- Claude の確認はビルドまで。テストサーバー(run-paper など)は使わず、起動もしない。実機での動作確認はユーザーが行う
- 実機での動作確認はサーバーを再起動して行う。PlugManX での読み込みは権限やコマンドの登録が不完全になることがある

## 最重要の設計方針

### TPS に影響させない
- 高頻度イベント(`EntityDamageByEntityEvent`、`PlayerInteractEvent`、`ChunkLoadEvent`、`CreatureSpawnEvent` など)は、安い判定(ワールド、MOB の種類、Infernal かどうかなど)を先に行い、対象外なら即座に抜ける
- `BlockPhysicsEvent`、`VehicleMoveEvent`、`PlayerMoveEvent` など発生頻度が極端に高いイベントは新たに使わない
- メインスレッドでファイルや DB の同期 I/O をしない(ただし、起動時・リロード時に一度だけ行う小さなファイルの読み込みは除く)。未読み込みのチャンクを判定のために読み込まない(`getChunkAtAsync`、`teleportAsync` を使う)
- 設定は起動時・リロード時に一度だけ解析して保持する。Material などの集合は EnumSet
- 定期タスクは最小限にし、追加するときは頻度と理由を設計案に書く

### 対応バージョン
- 最低対応は Paper 1.21.11(ビルドに使う API と同じ)。それより古いバージョン向けの書き分け(`GENERIC_MAX_HEALTH` など)は不要
- 1.21 未満向けの古い `config.yml` / `loot.yml` は、`LegacyConfigConverter` が起動時と `/im reload` のときに自動で変換する。Minecraft の更新で名前が変わったら、その変換表に追加する

### データの保存
- 設定とデータは `plugins/InfernalMobs/` の `config.yml`、`loot.yml`、`save.yml`(Infernal Mob の UUID と能力。チャンク読み込み時にここを見て能力を付け直す)
- 自動変換で書き換える前の控え: `config.yml.pre1.21.bak` / `loot.yml.pre1.21.bak`(すでにあれば日時付きの名前)
- プラグインフォルダ以外には何も書き込まない

### 他プラグインとの関係
- `softdepend: [Vault, WizardlyMagic]`
- 既存の機能(例: GSit、看板の `click_event` など)を妨げないこと。イベントをキャンセルする範囲は必要最小限にする

## 機能仕様

(現在の実装の動作。仕様を変えたらここを更新する)

- **設定ファイルの生成**: `config.yml` / `loot.yml` がなければ jar 内のものを書き出す(1 種類のみ)。サーバーのバージョンが変わっても設定は消さない(古い `configVersion` キーは使っていない)
- **古い設定ファイルの自動変換**(`LegacyConfigConverter`): 起動時と `/im reload` のときに、今のバージョンで使えない名前だけを変換表で置き換える。何かを置き換えたときだけ、控えを残してから保存し、置き換えた内容をログに出す。変換表にない無効な名前は書き換えずに警告だけ出す。何度実行しても結果は同じ
  - config.yml: `enabledmobs` / `enabledMounts` / `enabledRiders` / `disabledBabyMobs` と `mobChances` のキーの MOB 名(`PIG_ZOMBIE`→`ZOMBIFIED_PIGLIN`、`MUSHROOM_COW`→`MOOSHROOM`、`SNOWMAN`→`SNOW_GOLEM`)。置き換えで重複したら 1 つにまとめる
  - loot.yml: 効果名(`potionEffects.*.potion`、`consumeEffects.*.potionEffects`。`INCREASE_DAMAGE`→`STRENGTH` など 1.20.5 の改名)、ポーションの種類(`loot.*.potion`。`HEAL`→`HEALING` など)、アイテム名(`loot.*.item`。`WOOD_SWORD`→`WOODEN_SWORD` など)、エンチャント名(`loot.*.enchantments.*.enchantment`。`DAMAGE_ALL`→`SHARPNESS` など昔の Bukkit 名)
  - 有効かどうかは、実際にその値を読み込む処理と同じ方法で判定する(例: アイテムは `Material.valueOf`、効果は `infernal_mobs.getEffectType`)
- **`setloot` の保存形式**: 読み込み側(`getItem`)で読める形で保存する。ポーションは `PotionType` の名前(`PotionType.valueOf` に合わせる)、エンチャントは `sharpness` のような名前だけ(`NamespacedKey.minecraft` に合わせる)、耐久値は ItemMeta から読む。名前のないアイテムでは `name` を、持ち主のない頭では `owner` を保存しない(原作は耐久値の読み取りで ClassCastException になり、常に失敗していた)。すでにある番号に保存するときは、アイテムの中身に関する項目(`SETLOOT_ITEM_KEYS` と `lore0`〜)を先に消し、落とす条件(`mobs`、`powersMin` / `powersMax`、`chancePercentage`、`commands`)は残す
- **戦利品の名前・説明文**: 斜体にせずに表示する(原作は色コードのない部分だけ斜体だった)
- **コマンド**: `/infernalmobs`(別名 `/im`、権限 `infernal_mobs.commands`)。`reload`、`spawn` / `cspawn` / `pspawn`、`kill` / `killall`、`getloot` / `setloot` / `giveloot`、`setInfernal`、`abilities` / `showAbilities`、`mobs` / `mobList`、`info` / `worldInfo`、`help` など
- **撃破メッセージの `weapon`**: 武器に名前があればその名前、なければ種類名(`diamond sword` など)、素手なら `fist`(原作は名前のない武器だと空文字になっていた)
- **統計送信**: なし(サービスが終了していた旧 MCStats への送信処理は削除した)

## 過去にハマった点・注意点

- `config.getString(path, "")` のように既定値を渡すと、jar 内 config.yml の既定値が参照されない。既定値なしで取得して null を判定すること
- plugin.yml で `default: true` にした権限でも、登録されないと Bukkit は「OP のみ」として扱う。全員向けの機能を権限で縛らない
- IntelliJ の「アーティファクトのビルド」はクラスファイルが入らないことがある。必ず Gradle でビルドする
- paper-api 26.x は「JVM 25 以上専用」と宣言しているため、Java 21 で出力する設定ではビルドできない(1.21.11 でビルドしている理由)

## ファイル構成(src/main/java/io/hotmail/com/jacob_vejvoda/infernal_mobs/)

- `infernal_mobs.java`: メインクラス。起動処理、設定の生成、能力の付与、戦利品、コマンドなど大半の処理を持つ(約 2700 行)
- `EventListener.java`: イベント処理(出現、攻撃、死亡、チャンクの読み込み・解放など)
- `MobAbilities.java`: 能力の一部(mama など)の処理
- `InfernalMob.java`: Infernal Mob 1 体分のデータ
- `InfernalSpawnEvent.java`: 他のプラグイン向けの出現イベント(公開 API)
- `ArrowHomingTask.java`: 追尾する矢(1 tick ごとのタスク)
- `GUI.java`: 名前タグ・ボスバーなどの表示
- `LevelledEnchantment.java`: 戦利品のエンチャント(エンチャントとレベルの組)
- `VersionsHelper.java`: 最大体力の取得・設定
- `LegacyConfigConverter.java`: 1.21 未満の config.yml / loot.yml の古い名前を自動で変換する
- `LegacyText.java`: 原作の § 付き文字列と Adventure の Component の変換(推奨されない ChatColor・setDisplayName などの代わり)。アイテム名の比較は § 付き文字列に戻して行う(更新前に作られたアイテムとも一致させるため)
- `src/main/resources/`: `plugin.yml`、`config.yml`、`loot.yml`、`save.yml`
