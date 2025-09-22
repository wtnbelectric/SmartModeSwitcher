# 🚦 フェーズ3 開発仕様 & 開発順序ガイドライン

> **このドキュメントはフェーズ3（ジオフェンス・位置情報連動）の実装ガイドです。  
> 各Stepごとに「権限対応→Geofence導入→イベントハンドリング→ダッシュボード拡張→テスト」の順で進めてください。  
> 実装時は既存の時間帯ルールとの連携・優先順位ロジックに注意してください。**

## 🎯 フェーズ3の目的
- 登録済みの位置情報（緯度・経度・半径）を利用して **ジオフェンス判定を有効化**  
- 「時間帯ルールをベース」とし、「場所ルールで上書き」する挙動を実現する  

---

## ルール適用仕様（確定）

### 基本ロジック
- **時間帯ルールが常にベース**
- **場所ルールは時間帯ルールに上書き適用**

### フロー
1. **時間判定**
   - 現在時刻が一致する時間帯ルールを抽出  

2. **場所判定**
   - 該当する時間帯ルールの中で、位置情報が設定されているものをチェック  
   - Geofenceで「エリア内」にいる場合 → その場所ルールを優先適用  
   - エリア外に出た場合 → **ベースの時間帯ルールにリセット**  

### 優先順位
- 場所ルール ＞ 時間帯ルール  
- 場所ルール同士が競合した場合 → **最後に入ったエリアを優先**  

---

## ガントチャート反映（ダッシュボード）

- **時間帯ルール** → 通常の横棒表示  
- **場所ルール** → 横棒に 📍 アイコンやラベルを追加  
- **現在適用中のルール** → 太線や枠でハイライト表示  

---

## ユーザー体験例

- 09:00〜18:00 → マナーモード（時間帯ルール）  
- 📍会社（半径100m） → サイレント（場所ルール）  

挙動：
- 09:00〜18:00の間は「マナーモード」  
- 会社に入ると「サイレント」に上書き  
- 会社を出ると「マナーモード」にリセット  

---

## 開発順序（フェーズ3）

### Step 1. 権限対応

#### 1. 必要な権限をAndroidManifest.xmlに追加

- 実施済み

#### 2. 権限リクエスト処理（Activity/Fragment）

- FINE/COARSEは通常のランタイムパーミッションとしてリクエスト
- BACKGROUNDはAndroid 10+ の場合のみ追加でリクエスト

##### Kotlin例（Activity/Fragmentで実装）

- 実施済み

```kotlin
private val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

private fun checkAndRequestLocationPermissions() {
    val missing = locationPermissions.filter {
        ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
    }
    if (missing.isNotEmpty()) {
        requestPermissions(missing.toTypedArray(), REQUEST_CODE_LOCATION)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Android 10+ の場合、バックグラウンドも追加でリクエスト
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_CODE_LOCATION_BG)
        }
    }
}
```

#### 3. 権限結果のハンドリング

- 実施済み

```kotlin
override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    // 必要に応じて権限取得後の処理を実装
}
```

#### 4. ユーザーへの説明

- 権限が拒否された場合は、ダイアログ等で理由を説明し再リクエストを促す

### Step 2. Geofence導入
- `GeofencingClient` を利用  
- ルールごとに座標＋半径を登録  
- `BroadcastReceiver` で ENTER / EXIT イベントを受信  

### Step 3. イベントハンドリング
- ENTER → 場所ルールを適用（モード切替）  
- EXIT → ベースの時間帯ルールにリセット  

### Step 4. ダッシュボード拡張
- 「現在の位置に基づく有効ルール」を表示  
- ガントチャートで現在適用中のルールをハイライト  

### Step 5. テスト & 検証
- エリアに入る/出る時に正しく切替されるか  
- 競合ルール時に優先順位通りに動作するか  
- ダッシュボードに正しく反映されるか  

---

## 検索結果のハンドリング方針

- ジオフェンスや位置情報イベントは `BroadcastReceiver` で受信
- 受信結果は `ViewModel` で状態管理し、`Fragment`（例：DashboardFragment）でUIに反映
- ActivityからFragmentへはコールバックやViewModel共有で伝搬する
- UI層では「現在有効なルール」をハイライト表示する

---

## ✅ フェーズ3終了後にできること
- 登録した位置情報エリアに入ると自動でモードが切替  
- エリアを出るとベースの時間帯ルールにリセット  
- ダッシュボード上で「現在有効なルール」を直感的に確認可能
