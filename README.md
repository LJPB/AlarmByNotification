大きな音の代わりに通知で時間を知らせるアラームアプリ[(GooglePlayStore)](https://play.google.com/store/apps/details?id=me.ljpb.alarmbynotification)

AlarmByNotificationは通常のアラームの代わりに通知で時間を知らせるアプリです。

次の点にこだわりました：

1. アラームという名に相応しい働きをするように，設定した時間になったら遅延なく確実に通知を送れるようAlarmManagerで通知をスケジュールしています
2. 端末の再起動後に端末のロックを解除しなくてもAlarmManagerで通知を再度スケジュールできるように，通知に関する情報はデバイス暗号化ストレージに保存して，ダイレクトブートモードでDBにアクセスして通知を再度スケジュールしました
3. アプリアップデート後にAlarmManagerのスケジュールがリセットされないように，BroadcastRecieverでアップデートを検知して通知を再度スケジュールしました

バグ：

1. 通知設定後に端末を再起動した場合，通知が遅延したり送れなかったりする
    1. 現在原因調査中
