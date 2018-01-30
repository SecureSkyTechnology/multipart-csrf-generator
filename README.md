# multipart-csrf-generator

- CSRF検査用のフォームを生成するWebツールです。
- `application/x-www-form-urlencoded` および `multipart/form-data` に対応しています。
- multipartの場合は、JavaScriptで送信するコードを生成します。その際、XHRのwithCredentialsとFormDataオブジェクトを使いますので、ブラウザの対応状況に注意してください。
  - https://caniuse.com/#search=FormData
  - https://caniuse.com/#search=XHR2

## 使い方

1. Java8 のJREまたはJDKをインストールし、シェルまたはコマンドプロンプトからjavaコマンドを実行できるようにしておきます。
2. GitHubのリリースページより、jarファイルをダウンロードします。
3. シェルまたはコマンドプロンプトを開き、 `java -jar (jarファイル名)` を実行します。
4. ブラウザで http://127.0.0.1:9002/ にアクセスします。
5. CSRFを検査したいHTTPリクエストをファイルに保存し、フォームにアップロードすると、CSRF検査用HTMLを生成します。

ポート番号を変更したい場合は、以下のようにポート番号を指定して実行します。
```
java -jar (jarファイル名) --server.port=(ポート番号)
```

## requirement

* Java8

## 開発環境

* JDK >= 1.8.0_92
* STS(Spring Tool Suites &trade;) >= 3.8
  * Spring Boot アプリケーションなのでSTSでの開発を推奨します。
* Maven >= 3.5.2 (maven-wrapperにて自動的にDLしてくれる)
* ソースコードやテキストファイル全般の文字コードはUTF-8を使用

## ビルドと実行

```
cd multipart-csrf-generator/

ビルド:
mvnw package

jarファイルから実行:
java -jar target/multipart-csrf-generator-xxx.jar
```

## STSプロジェクト用の設定

* STSのダウンロードとインストールについては公式サイト参照のこと
  * https://spring.io/tools/sts

* https://github.com/SecureSkyTechnology/howto-eclipse-setup の `setup-type1` を使用。README.mdで以下を参照のこと:
* Clean Up/Formatter 設定
* 必須プラグイン
  * Lombok
* GitでcloneしたMavenプロジェクトのインポート 
