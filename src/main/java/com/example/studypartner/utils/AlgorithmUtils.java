package com.example.studypartner.utils;

import com.hankcs.hanlp.HanLP;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author wuxie
 * 算法工具类
 */
public class AlgorithmUtils {

    /**
     * 编辑距离算法
     * @param tagList1
     * @param tagList2
     * @return
     */
    public static int minDistance(List<String> tagList1, List<String> tagList2) {
        int n = tagList1.size();
        int m = tagList2.size();

        if (n * m == 0) {
            return n + m;
        }

        int[][] d = new int[n + 1][m + 1];
        for (int i = 0; i < n + 1; i++) {
            d[i][0] = i;
        }

        for (int j = 0; j < m + 1; j++) {
            d[0][j] = j;
        }

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < m + 1; j++) {
				int left = d[i - 1][j] + 1;
				int down = d[i][j - 1] + 1;
				int leftDown = d[i - 1][j - 1] + customCost(tagList1.get(i - 1), tagList2.get(j - 1));
				d[i][j] = Math.min(left, Math.min(down, leftDown));
            }
        }
        return d[n][m];
    }
	private static int customCost(String tag1, String tag2) {
		// 使用分词工具对中文标签进行分词
		List<String> words1 = HanLP.segment(tag1).stream().map(term -> term.word).collect(Collectors.toList());
		List<String> words2 = HanLP.segment(tag2).stream().map(term -> term.word).collect(Collectors.toList());

		// 比较分词后的标签
		int commonWords = (int) words1.stream().filter(words2::contains).count();
		return Math.max(words1.size(), words2.size()) - commonWords;
	}
}
