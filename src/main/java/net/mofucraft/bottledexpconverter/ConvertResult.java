package net.mofucraft.bottledexpconverter;

import org.jetbrains.annotations.Nullable;

/**
 * BottledExp変換の結果を表すレコード
 */
public record ConvertResult(
        boolean success,
        boolean disabled,
        boolean alreadyConverted,
        long storedExp,
        @Nullable String errorMessage
) {

    /**
     * 成功結果を作成
     */
    public static ConvertResult success(long storedExp) {
        return new ConvertResult(true, false, false, storedExp, null);
    }

    /**
     * 失敗結果を作成
     */
    public static ConvertResult failure(String errorMessage) {
        return new ConvertResult(false, false, false, -1, errorMessage);
    }

    /**
     * 無効化されている場合の結果を作成
     */
    public static ConvertResult notEnabled() {
        return new ConvertResult(false, true, false, -1, "BottledExp変換は無効化されています");
    }

    /**
     * 既に変換済みの場合の結果を作成
     */
    public static ConvertResult alreadyConverted(long storedExp) {
        return new ConvertResult(false, false, true, storedExp,
                "既にCMI形式に変換済みです (Exp: " + storedExp + ")");
    }

    /**
     * 結果の説明文を取得
     */
    public String getMessage() {
        if (success) {
            return "変換成功: Stored Exp = " + storedExp;
        }
        return errorMessage != null ? errorMessage : "不明なエラー";
    }
}
