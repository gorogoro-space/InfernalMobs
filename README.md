# InfernalMobs
[![Paper 1.21.11](https://img.shields.io/badge/Paper-1.21.11-brightgreen.svg)](https://fill-ui.papermc.io/projects/paper/version/1.21.11)
[![GitHub release](https://img.shields.io/github/release/gorogoro-space/InfernalMobs.svg)](https://github.com/gorogoro-space/InfernalMobs/releases)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/gorogoro-space/InfernalMobs/issues)
[![License: CC BY-NC-SA 2.5 CA](https://img.shields.io/badge/License-CC%20BY--NC--SA%202.5%20CA-blue.svg)](https://creativecommons.org/licenses/by-nc-sa/2.5/ca/)

## 動作環境

- Paper 1.21.11 以降。Spigot と 1.21.11 未満のサーバーには対応していません(読み込まれません)
- Java 21 必須(サーバーの実行とビルドの両方。ビルドには JDK 21 が必要です。Gradle は同梱の Gradle Wrapper(9.8.0)を使うので、インストールは不要です)

## コマンド

コマンドは `/infernalmobs`(別名 `/im`)です。

**すべてのコマンドは OP 専用です。** どのコマンドも権限 `infernal_mobs.commands` で判定しており、この権限は既定で OP だけが持っています。OP 以外のプレイヤーに使わせたい場合は、権限プラグイン(LuckPerms など)で `infernal_mobs.commands` を与えてください(与えるとすべてのコマンドが使えるようになります。コマンドごとには分けられません)。

「コンソール」欄が ○ のものはサーバーのコンソールからも実行できます。それ以外はプレイヤーだけが実行できます。

| コマンド | 説明 | コンソール | OP 専用 |
|---|---|:---:|:---:|
| `/im help` | コマンドの一覧を表示します | | ○ |
| `/im reload` | `config.yml` と `loot.yml` を読み込み直します。1.21 未満の古い名前があれば自動で変換します | ○ | ○ |
| `/im spawn <MOB>` | 見ている場所に、ランダムな能力を持った Infernal Mob を出します | | ○ |
| `/im spawn <MOB> <能力> [<能力> ...]` | 見ている場所に、指定した能力を持った Infernal Mob を出します | | ○ |
| `/im cspawn <MOB> <ワールド> <x> <y> <z> [<能力> ...]` | 指定した座標に Infernal Mob を出します。能力を省略するとランダムになります | ○ | ○ |
| `/im pspawn <MOB> <プレイヤー> <能力> [<能力> ...]` | 指定したプレイヤーの位置に、指定した能力を持った Infernal Mob を出します | ○ | ○ |
| `/im kill <半径>` | 自分の周り(半径ブロック以内)の Infernal Mob を消します | | ○ |
| `/im killall [<ワールド>]` | ワールド内の読み込まれている Infernal Mob をすべて消します。コンソールではワールド名が必要です | ○ | ○ |
| `/im getloot` | ランダムな戦利品を 1 つ受け取ります | | ○ |
| `/im getloot <番号>` | `loot.yml` の指定した番号の戦利品を受け取ります | | ○ |
| `/im giveloot <プレイヤー> <番号>` | 指定した番号の戦利品をプレイヤーに渡します | ○ | ○ |
| `/im setloot <番号>` | 手に持っているアイテムを、`loot.yml` の指定した番号の戦利品として保存します | | ○ |
| `/im abilities` | 能力の一覧を表示します | | ○ |
| `/im showAbilities` | 見ている Infernal Mob の能力を表示します | | ○ |
| `/im setInfernal <秒>` | 見ているスポナーを「Infernal Mob が出るスポナー」にします(秒は出現の間隔) | | ○ |
| `/im worldInfo` | 今いるワールドで Infernal Mob が出るかどうかと、出るワールドの一覧を表示します | | ○ |
| `/im mobList` | コマンドの `<MOB>` に指定できる名前(`zombie` のような小文字)の一覧を表示します | | ○ |
| `/im mobs` | `config.yml` の `enabledmobs` などに書く名前(`ZOMBIE` のような大文字)の一覧を表示します | | ○ |
| `/im info` | 管理中の Infernal Mob・乗り物の数などを表示します(調査用) | | ○ |
| `/im error` | 次に右クリックした MOB の情報(名前・保存された能力・体力など)を表示します(調査用) | | ○ |
| `/im slotTest` | `enabledCharmSlots` のスロットに赤い色付きガラス板を置き、位置を確かめます(調査用) | | ○ |

- `<MOB>` は `zombie` や `wither_skeleton` のような MOB の名前です(大文字・小文字は区別しません)
- `<能力>` は下の「能力の一覧」にある名前です
- 戦利品の `<番号>` は `loot.yml` の `loot:` の下の番号です

## 設定ファイル

設定ファイルは `plugins/InfernalMobs/` にあります。初回起動時に作られます。変更したら `/im reload` で読み込み直せます。

- `config.yml`: Infernal Mob の出現、能力、表示などの設定
- `loot.yml`: 戦利品と、戦利品を持ったときの効果の設定
- `save.yml`: Infernal Mob の能力と、Infernal Mob が出るスポナーの記録(プラグインが書き込むので、手で編集しないでください)

文字の色は `&` で始まる色コード(例: `&c` は赤、`&l` は太字)で指定できます。

### config.yml

#### 出現

| 項目 | 初期値 | 説明 |
|---|---|---|
| `chance` | `100` | MOB が出現したときに Infernal Mob になる確率。「1/この値」の確率になります(小さいほど出やすい。最小 1) |
| `mobChances` | (なし) | MOB ごとの確率。例: `GHAST: 150` でガストだけ 1/150 にします |
| `levelChance` | (なし) | 能力の数ごとの追加の確率。「1/(この値 − 1)」の確率になります。例: `'7': 5` で、能力が 7 個になった場合はさらに 1/4 の確率でしか出ません |
| `mobworlds` | `<all>` | Infernal Mob が出るワールド。`<all>` ですべてのワールド |
| `enabledmobs` | (MOB の一覧) | Infernal Mob になる MOB。名前は `ZOMBIE` のように大文字で書きます。行頭の `#` を外すと有効になります |
| `enabledSpawnReasons` | (理由の一覧) | Infernal Mob になる出現理由(`NATURAL` は自然湧き、`SPAWNER` はスポナーなど)。行頭の `#` を外すと有効になります |
| `naturalSpawnHeight` | `0` | この高さ(Y 座標)より上で出現した MOB だけが Infernal Mob になります |
| `disabledBabyMobs` | (MOB の一覧) | 子どもの場合は Infernal Mob にしない MOB |

#### 能力

| 項目 | 初期値 | 説明 |
|---|---|---|
| `minpowers` / `maxpowers` | `3` / `7` | 1 体に付く能力の数の最小・最大。有効な能力の数より大きくしないでください |
| `powerByDistance` | `false` | `true` にすると、能力の数をワールドのスポーン地点からの距離で決めます(`addDistance` ブロックごとに `powerToAdd` 個) |
| `powerToAdd` | `1` | `powerByDistance` のときに、距離ごとに増える能力の数 |
| 能力の名前(`poisonous` など) | `true` | 能力ごとの有効/無効。`false` にするとその能力は付きません(下の「能力の一覧」を参照) |
| `effectAllPlayerAttacks` | `true` | `true` にすると、能力の効果がプレイヤー以外(飼い犬など)との戦いでも起きます |
| `mamaSpawnAmount` | `3` | `mama` で出てくる子どもの数 |
| `vengeanceDamage` | `6` | `vengeance` の反撃のダメージ(ハート数) |
| `berserkDamage` | `3` | `berserk` の追加ダメージ(ハート数) |
| `moltenBurnLength` | `5` | `molten` で燃える秒数 |
| `gravityLevitateLength` | `6` | `gravity` で浮かされる秒数 |
| `fireworkColour` | `255, 10, 10` | `firework` の花火の色(`red` / `green` / `blue`) |

#### 体力・経験値

| 項目 | 初期値 | 説明 |
|---|---|---|
| `healthMultiplier` | `4` | 体力を元の何倍にするか |
| `healthByPower` | `false` | `true` にすると、体力を「元の体力 × 能力の数」にします(`healthMultiplier` と `healthByDistance` は無視されます) |
| `healthByDistance` | `false` | `true` にすると、体力をワールドのスポーン地点からの距離で決めます(`addDistance` ブロックごとに `healthToAdd`) |
| `healthToAdd` | `5` | `healthByDistance` のときに、距離ごとに増える体力 |
| `addDistance` | `200` | `healthByDistance` と `powerByDistance` で使う距離の単位(ブロック) |
| `xpMultiplier` | `8` | 落とす経験値を元の何倍にするか |

#### 戦利品

| 項目 | 初期値 | 説明 |
|---|---|---|
| `enableDrops` | `true` | 倒したときに `loot.yml` の戦利品を落とすか |
| `dropChance` | `1` | 戦利品を落とす確率。「1/この値」の確率になります(1 なら必ず落とす) |
| `enableFarmingDrops` | `false` | `true` にすると、プレイヤー以外(トラップなど)に倒された場合も戦利品を落とします |
| `noCreativeDrops` | (なし) | `true` を書き足すと、クリエイティブモードのプレイヤーが倒した場合は戦利品を落としません |
| `effectworlds` | `<all>` | 戦利品を持ったときの効果(`loot.yml` の `potionEffects`)が働くワールド |
| `enabledCharmSlots` | `0`〜`8`, `36`〜`40` | 効果が働くインベントリのスロット番号(0〜8 はホットバー、36〜39 は防具、40 はオフハンド) |

#### 乗り物

| 項目 | 初期値 | 説明 |
|---|---|---|
| `enabledRiders` | (MOB の一覧) | `mounted` の能力で乗り物に乗れる MOB |
| `enabledMounts` | (MOB の一覧) | `mounted` で乗り物として使われる MOB |
| `horseMountsHaveSaddles` | `true` | 乗り物の馬に鞍を付けるか |
| `armouredMountsHaveArmour` | `true` | `armoured` の能力を持つ場合に、乗り物の馬に鎧を付けるか |
| `mountFate` | `nothing` | 乗っていた Infernal Mob が倒されたときの乗り物の扱い。`nothing`(そのまま)/ `death`(倒す)/ `removal`(消す) |

#### 表示

| 項目 | 初期値 | 説明 |
|---|---|---|
| `namePrefix` | `&fInfernal` | 名前の前に付ける文字 |
| `levelPrefixs` | (なし) | 能力の数ごとの前置き。例: `'5': '&fGiant'` |
| `nameTagsLevel` | `1` | 頭上の名前の表示。`0` は表示しない、`1` は見ているときだけ、`2` は常に表示。`0` にするとサーバーの再起動で Infernal Mob が普通の MOB に戻ります |
| `nameTagsName` | `&f<prefix> <mobName>` | 頭上の名前の書式 |
| `enableBossBar` | `true` | 近くの Infernal Mob の体力をボスバーで表示するか |
| `bossBarsName` | `&fLevel <mobLevel> &f<prefix> <mobName>` | ボスバーの書式 |
| `bossBarSettings` | `PINK` / `SOLID` | ボスバーの色(`defaultColor`)と形(`defaultStyle`)。`perMob`(MOB ごと)と `perLevel`(能力の数ごと)で個別に変えられます |
| `enableScoreBoard` | `false` | 近くの Infernal Mob の能力をサイドバーに表示するか |
| `showHealthOnScoreBoard` | `true` | サイドバーに体力も表示するか |
| `enableParticles` | `true` | Infernal Mob の周りにパーティクルを出すか |
| `mobParticles` | `lavaSpark:1:10` | パーティクルの種類と量(`種類:速さ:数`)。複数書くと 1 体ごとにランダムに選びます。種類は `lavaSpark`、`flame`、`smoke`、`largeSmoke`、`cloud`、`heart`、`ender`、`criticalHit`、`magicCriticalHit`、`enchantmentTable`、`greenSparkle`、`angryVillager`、`noteBlock`、`splash`、`potionBrake`、`blockBrake`、`hugeExplode`、`explode`、`largeExplode`、`mobSpell`、`witchMagic`、`tileDust`、`colouredDust` |
| `enableSpawnMessages` | `false` | Infernal Mob が出たときにメッセージを出すか |
| `spawnMessages` | (メッセージの一覧) | 出現メッセージ。この中からランダムに 1 つ選びます |
| `spawnMessageRadius` | `64` | 出現メッセージを送る範囲(ブロック)。`-1` で同じワールド全員、`-2` でサーバー全員 |
| `enableDeathMessages` | `false` | Infernal Mob が倒されたときに全体へメッセージを出すか |
| `deathMessages` | (メッセージの一覧) | 撃破メッセージ。`player` が倒した人、`mob` が MOB、`weapon` が武器の名前(名前を付けていない武器は `diamond sword` のような種類名、素手は `fist`)に置き換わります |

書式の中の `<prefix>` は `namePrefix`(または `levelPrefixs`)、`<mobName>` は MOB の名前、`<mobLevel>` は能力の数、`<abilities>` は能力の名前(入るだけ)に置き換わります。

#### 能力の一覧

| 能力 | 効果 |
|---|---|
| `1up` | 体力が少なくなると、1 回だけ全回復します |
| `archer` | 矢を 3 本まとめて放ちます。矢は近くの相手を追いかけます |
| `armoured` | 防具を身に着けます(ゾンビ・スケルトン以外は耐性の効果) |
| `berserk` | 自分の体力を削って、大きなダメージを与えます |
| `blinding` | 攻撃した相手に盲目の効果を与えます |
| `bullwark` | 攻撃を受けると耐性の効果を得ます |
| `cloaked` | 透明になります |
| `confusing` | 攻撃した相手に吐き気の効果を与えます |
| `ender` | 攻撃を受けると相手の近くへテレポートします |
| `explode` | 倒されると爆発します |
| `firework` | 攻撃を受けると花火を打ち上げます |
| `flying` | 透明なコウモリに乗って飛びます |
| `ghastly` | 近くのプレイヤーに火の玉を撃ちます |
| `ghost` | 倒されると幽霊(透明なゾンビ)が出てきます |
| `gravity` | 近くのプレイヤーを浮遊させます |
| `lifesteal` | 攻撃すると体力が回復します |
| `mama` | 攻撃を受けると子どもを産みます(数は `mamaSpawnAmount`) |
| `molten` | 火炎耐性を持ち、戦った相手を燃やします |
| `morph` | 攻撃を受けると、まれに別の MOB に姿を変えます |
| `mounted` | 乗り物に乗って出てきます |
| `necromancer` | 近くのプレイヤーにウィザーの頭蓋骨を撃ちます |
| `poisonous` | 攻撃した相手に毒の効果を与えます |
| `potions` | 相手にスプラッシュポーションを投げます |
| `quicksand` | 相手に鈍化の効果を与えます |
| `rust` | 相手が手に持っている道具の耐久値を減らします |
| `sapper` | 攻撃した相手に空腹の効果を与えます |
| `sprint` | 移動速度が上がります |
| `storm` | 相手に雷を落とします |
| `thief` | 相手が手に持っているアイテムを落とさせます |
| `tosser` | 近くのプレイヤーを引き寄せます(スニーク中は効きません) |
| `vengeance` | 攻撃を受けると反撃のダメージを与えます |
| `weakness` | 相手に弱体化の効果を与えます |
| `webber` | 相手の足元にクモの巣を張ります |
| `withering` | 攻撃した相手に衰弱の効果を与えます |

### loot.yml

`loot.yml` は 3 つの部分に分かれています。

#### loot(戦利品)

`loot:` の下に、番号ごとに戦利品を書きます。Infernal Mob が倒されると、条件に合う戦利品の中からランダムに 1 つ選ばれます。

```yaml
loot:
  '1':
    item: IRON_AXE
    amount: 1
    name: '&2Murder Axe'
    lore1: '&aFor felling the undead!'
    unbreaking: 8
    enchantments:
      '1':
        enchantment: SMITE
        level: 6
```

| 項目 | 説明 |
|---|---|
| `item` | アイテムの名前(`DIAMOND_SWORD` など。大文字) |
| `amount` | 個数。`1-5` のように書くと範囲内でランダム |
| `name` | 表示名。リストで書くとランダムに 1 つ選びます。`<itemName>` はアイテムの名前に置き換わります |
| `lore1`〜`lore32` | 説明文(1 行ずつ) |
| `lore` / `minLore` / `maxLore` | 説明文の候補のリストと、そこから選ぶ行数の最小・最大 |
| `durability` | 減らしておく耐久値。`1-50` のような範囲も書けます |
| `unbreaking` | 耐久力のエンチャントのレベル |
| `enchantments` | エンチャント。番号ごとに `enchantment`(名前)、`level`(レベル。範囲も可)、`chance`(付く確率 1/この値)を書きます |
| `minEnchantments` / `maxEnchantments` | 付けるエンチャントの数の最小・最大 |
| `potion` | ポーションの種類(`item` がポーションのとき。`HEALING`、`STRENGTH` など) |
| `colour` | 革の防具の色(`赤,緑,青`)。盾では色の名前(`RED` など) |
| `patterns` | 旗と盾の模様(`/im setloot` で保存したもの) |
| `owner` | プレイヤーの頭の持ち主(UUID) |
| `author` / `title` / `pages` | 本の著者・題名・ページ |
| `mobs` | この戦利品を落とす MOB(`zombie` のような小文字の名前のリスト)。書かなければすべての MOB |
| `powersMin` / `powersMax` | この戦利品を落とす Infernal Mob の能力の数の範囲 |
| `chancePercentage` | 候補に入る確率(%) |
| `commands` | 倒したときにコンソールで実行するコマンド。`player` は倒した人の名前に置き換わります |

`name` と説明文は斜体にせずに表示します(原作では、色コード `&a` などを付けていない部分だけが斜体になっていました)。

#### potionEffects(持ったときの効果)

指定した戦利品を持っている(または身に着けている)間、効果が続きます。

| 項目 | 説明 |
|---|---|
| `potion` | 効果の名前(`STRENGTH`、`SPEED` など) |
| `level` | 効果のレベル |
| `requiredItems` | 必要な戦利品の番号のリスト。すべてを持っているときに効果が出ます |
| `particleEffect` | 効果中に出すパーティクル(`種類:速さ:数`) |
| `attackEffect` | 書いた場合は、その戦利品で攻撃したときだけ効果が出ます。`target` は攻撃した相手に、`self` は自分に効果を与えます |
| `attackHelpEffect` | 書いた場合は、その戦利品を持って攻撃したときに効果が出ます(`target` / `self`) |

#### consumeEffects(食べたときの効果)

指定した戦利品を食べたり飲んだりしたときの効果です。

| 項目 | 説明 |
|---|---|
| `requiredItem` | 対象の戦利品の番号 |
| `message` | 食べたときに表示するメッセージ |
| `potionEffects` | 与える効果のリスト。`効果の名前:レベル:秒数` の形で書きます(`fertility` はこのプラグイン独自の効果) |

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

* **生成先:** `build/libs/InfernalMobs-<version>.jar`(例: `InfernalMobs-7.0.1.jar`。バージョンは `build.gradle` の `version`)

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
