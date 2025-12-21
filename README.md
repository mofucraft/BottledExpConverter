# BottledExpConverter

旧形式のBottledExp（経験値ボトル）をCMI互換形式に変換するBukkitプラグインです。

## 変換内容

### 変換前（旧形式）
```snbt
{
  count: 1,
  id: "minecraft:experience_bottle",
  components: {
    "minecraft:custom_data": {
      StoredBottledExp: 4339720L,
      PublicBukkitValues: {
        "cmistoredbottledexp": 4339720L
      }
    },
    "minecraft:custom_name": '...',
    "minecraft:lore": ['Stored Exp: 4339720']
  }
}
```

### 変換後（CMI互換形式）
```snbt
{
  count: 1,
  id: "minecraft:experience_bottle",
  components: {
    "minecraft:custom_data": {
      PublicBukkitValues: {
        "cmilib:storedbottledexp": 4339720
      }
    },
    "minecraft:custom_name": '...',
    "minecraft:lore": ['Stored Exp: 4339720']
  }
}
```

### 変換ポイント
| 項目 | 変換前 | 変換後 |
|------|--------|--------|
| キー名 | `cmistoredbottledexp` | `cmilib:storedbottledexp` |
| データ型 | Long (`L`付き) | Integer |
| 不要キー | `StoredBottledExp` | 削除 |

## コマンド

| コマンド | 説明 | 権限 |
|----------|------|------|
| `/bexpconvert convert` | 手持ちのBottledExpを変換 | `bottledexpconverter.convert` |
| `/bexpconvert reload` | 設定を再読み込み | `bottledexpconverter.reload` |
| `/bexpconvert status` | ステータスを表示 | `bottledexpconverter.convert` |

エイリアス: `/bexpc`

## 設定 (config.yml)

```yaml
# 自動変換を有効にするかどうか
# true: 以下のタイミングで自動変換
#   - プレイヤーがログインした時（インベントリ全体）
#   - アイテムを拾った時
#   - インベントリでアイテムをクリックした時
#   - アイテムを使用しようとした時
# false: /bexpconvert convert コマンドでのみ変換
# デフォルト: false（安全のため手動変換推奨）
auto-convert: false

# デバッグモード（詳細なログを出力）
debug: false

# 変換時にプレイヤーにメッセージを送信するかどうか
notify-player: true
```

## インストール

1. [Releases](https://github.com/mofucraft/BottledExpConverter/releases) から最新のJARをダウンロード
2. `plugins` フォルダに配置
3. サーバーを再起動

## 動作要件

- Minecraft 1.21+
- Paper/Spigot サーバー

## ビルド

```bash
./gradlew build
```

JARファイルは `build/libs/` に生成されます。

## ライセンス

MIT License
