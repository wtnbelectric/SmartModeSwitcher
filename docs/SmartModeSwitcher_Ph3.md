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

- 実施済み

- 権限が拒否された場合は、ダイアログ等で理由を説明し再リクエストを促す

### Step 2. Geofence導入

#### 1. GeofencingClientの初期化

- 実施済み

```kotlin
val geofencingClient = LocationServices.getGeofencingClient(context)
```

#### 2. Geofenceリストの作成

- 実施済み

```kotlin
val geofence = Geofence.Builder()
    .setRequestId("rule_${rule.id}")
    .setCircularRegion(rule.latitude, rule.longitude, rule.radius)
    .setExpirationDuration(Geofence.NEVER_EXPIRE)
    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
    .build()
```

#### 3. GeofencingRequestの作成

- 実施済み

```kotlin
val geofencingRequest = GeofencingRequest.Builder()
    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
    .addGeofence(geofence)
    .build()
```

#### 4. PendingIntentの用意

- 実施済み

```kotlin
val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
val pendingIntent = PendingIntent.getBroadcast(
    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)
```

#### 5. Geofenceの登録

- 実施済み

```kotlin
geofencingClient.addGeofences(geofencingRequest, pendingIntent)
    .addOnSuccessListener { /* 登録成功 */ }
    .addOnFailureListener { e -> /* エラー処理 */ }
```

#### 6. BroadcastReceiverの実装

- 実施済み

- `GeofenceBroadcastReceiver` を作成し、`onReceive` で ENTER/EXIT を判定
- イベント内容をViewModelやActivityに伝搬

---

**実装例や詳細は公式ドキュメントも参照：**  
https://developer.android.com/training/location/geofencing

### Step 3. イベントハンドリング

#### 実装方法

1. **BroadcastReceiverでGeofenceイベントを受信**
   - `GeofenceBroadcastReceiver` の `onReceive` で ENTER/EXIT を判定
   - `requestId` から該当ルールIDを取得

2. **イベントをアプリ本体に伝搬**

- 実施済み

   - 例: `IntentService` や `ViewModel`、`Activity` へブロードキャストやシングルトン経由で通知

3. **ルール適用処理**

- 実施済み

   - ENTER: 場所ルールを適用（モード切替など）
   - EXIT: ベースの時間帯ルールにリセット

4. **UI反映**

- 実施済み

   - `DashboardFragment` などで「現在有効なルール」をハイライト表示

#### サンプルフロー

- `GeofenceBroadcastReceiver` → `MainActivity.onRuleSearchResult(ruleId)` → `DashboardFragment.handleRuleSearchResult(ruleId)` → ViewModel/Adapter/UI更新

### Step 4. ダッシュボード拡張
- 「現在の位置に基づく有効ルール」を表示  
- ガントチャートで現在適用中のルールをハイライト  

### Step 5. テスト & 検証

1. **エリアに入る/出る時に正しく切替されるか**
   - 端末の位置情報を使い、登録したスポットのエリアに実際に入る/出る動作を確認
   - ジオフェンスイベント（ENTER/EXIT）が発生し、ルールが正しく適用・リセットされるかをダッシュボードで確認

2. **競合ルール時に優先順位通りに動作するか**
   - 複数の場所ルールが重なる場合、最後に入ったエリアが優先されるかを確認
   - エリアを出た場合、時間帯ルールに戻るかを確認

3. **ダッシュボードに正しく反映されるか**
   - 現在有効なルールがガントチャートやリストでハイライトされるか
   - スポット名や座標が正しく表示されているか

4. **地図選択・スポット登録の動作確認**
   - 地図からスポットを選択し、スポット名・座標がルールに反映されるか
   - ルール編集画面で値が正しくセットされるか

5. **権限リクエスト・エラー時の挙動**
   - 位置情報権限を拒否した場合、ユーザーへの説明や再リクエストが正しく表示されるか

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

---

## 🗺️ 新機能要望：地図からスポット選択・座標取得による場所設定

- ルール作成・編集時に「地図から場所を選択」できるUIを追加する
- Google Maps等の地図上でピンをタップ、または長押しで座標（緯度・経度）を取得し、ルールに反映
- 既存の緯度・経度入力欄と連動し、地図で選択した値が自動で反映される
- 既存スポット（例：自宅・会社）をリストから選ぶ方式も検討

---

### 🛠️ 実装方法（案）

1. **Google Maps APIの導入**
   - `build.gradle`に`maps`依存を追加し、APIキーを取得・設定
2. **地図選択用Fragment/Activityの作成**
   - ルール編集画面から「地図で選択」ボタンを設置
   - ボタン押下で地図画面（MapSelectFragmentなど）を起動
3. **地図上でピン設置・座標取得**
   - 地図をタップまたは長押しでピンを表示し、緯度・経度を取得
   - 決定ボタンで選択座標をルール編集画面に返す（FragmentResultやIntentで値渡し）
   - **スポットを選択した場合は、そのスポットの「名前」と「座標（緯度・経度）」をセットで登録する**
4. **ルール編集画面で値を反映**
   - 受け取った座標とスポット名を緯度・経度欄・スポット名欄に自動入力
   - 必要に応じて半径も地図上で指定できるUIを追加

---

### 🗺️ OpenStreetMap(osmdroid) 導入手順

1. **build.gradleに依存関係を追加**

- 実施済み

   - `libs.versions.toml` または `build.gradle` の dependencies に以下を追加
   ```groovy
   implementation 'org.osmdroid:osmdroid-android:6.1.16'
   ```

2. **AndroidManifest.xmlに権限と設定を追加**
   ```xml

- 実施済み

   <uses-permission android:name="android.permission.INTERNET"/>
   <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
   <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
   <application>
       <!-- osmdroidのキャッシュディレクトリ設定（推奨） -->
       <meta-data android:name="osmdroid.basePath" android:value="osmdroid"/>
       <meta-data android:name="osmdroid.cachePath" android:value="osmdroid"/>
       <!-- ... -->
   </application>
   ```

3. **地図表示用のMapViewをレイアウトに追加**
   - 例:
   ```xml
   <org.osmdroid.views.MapView
       android:id="@+id/map"
       android:layout_width="match_parent"
       android:layout_height="match_parent"/>
   ```

4. **Activity/FragmentでMapViewを初期化**
   - サンプルKotlinコード
   ```kotlin
   import org.osmdroid.config.Configuration
   import org.osmdroid.views.MapView

   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
       Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
       setContentView(R.layout.your_layout)
       val map = findViewById<MapView>(R.id.map)
       map.setMultiTouchControls(true)
       // 必要に応じて初期位置やズームを設定
   }
   ```

---

## 🗺️ 地図APIについて

Google Maps Platformは商用・長期利用では無料枠に制限がありますが、  
**完全無料の地図APIはAndroid公式では存在しません。**

### 主な選択肢

- **Google Maps Platform**  
  - 月200ドル分まで無料枠あり（超過は課金）
  - Android公式サポート・ドキュメントが豊富

- **OpenStreetMap + サードパーティライブラリ**
  - [osmdroid](https://github.com/osmdroid/osmdroid)（Android用のOpenStreetMap表示ライブラリ、無料・APIキー不要）
  - [Mapsforge](https://github.com/mapsforge/mapsforge) など
  - 商用利用やカスタマイズも可能だが、公式サポートや機能はGoogle Mapsより限定的

### 注意点

- 完全無料で使いたい場合は **osmdroid** などOpenStreetMap系ライブラリを検討
- ただし、地図の見た目や機能（ピン設置、ジオコーディング等）はGoogle Mapsより劣る場合がある
- ジオフェンスや位置情報API自体はGoogle Play Services依存なので、地図表示だけをOpenStreetMapにすることは可能
