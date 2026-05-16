package com.example.gomplay.global.util;

public class CollegeMapper {

    public static String getCollege(String department) {
        if (department == null) return "기타";

        return switch (department) {
            // 문과대학
            case "국어국문학과", "사학과", "철학과", "영미인문학과" -> "문과대학";

            // 법과대학
            case "법학과" -> "법과대학";

            // 사회과학대학
            case "정치외교학과", "행정학과", "상담학과",
                 "도시지역계획학", "부동산학",
                 "저널리즘", "영상콘텐츠", "광고홍보" -> "사회과학대학";

            // 경영경제대학
            case "경제학과", "무역학과", "경영학", "회계학", "산업경영학과" -> "경영경제대학";

            // 공과대학
            case "전자전기공학과", "융합반도체공학과", "인프라건설공학과",
                 "기계공학과", "화학공학과",
                 "고분자공학", "융합소재공학",
                 "건축학", "건축공학" -> "공과대학";

            // AI융합대학
            case "소프트웨어학과", "컴퓨터공학과", "통계데이터사이언스학과",
                 "사이버보안학과", "인공지능학과",
                 "SW융합바이오", "SW융합콘텐츠", "SW융합경제경영",
                 "SW융합법학", "글로벌SW융합" -> "AI융합대학";

            // 음악예술대학
            case "도예과", "무용과",
                 "커뮤니케이션디자인", "패션산업디자인",
                 "연극", "영화", "뮤지컬",
                 "피아노", "관현악", "성악", "작곡", "국악" -> "음악예술대학";

            // 사범대학
            case "한문교육과", "특수교육과", "수학교육과", "체육교육과",
                 "물리", "생물" -> "사범대학";

            // 프리무스국제대학
            case "국제경영학과", "모바일시스템공학과", "바이오소재융합공학과",
                 "한국학과", "연기영상예술학과", "글로벌기초교육학부" -> "프리무스국제대학";

            // 퇴계혁신칼리지
            case "퇴계혁신칼리지" -> "퇴계혁신칼리지";     

            default -> "기타";
        };
    }

    public static int getCollegeScore(String dept1, String dept2) {
        if (dept1 == null || dept2 == null) return 0;
        if (dept1.equals(dept2)) return 5; // 같은 학과
        
        String college1 = getCollege(dept1);
        String college2 = getCollege(dept2);
        
        if (college1.equals(college2) && !college1.equals("기타")) return 3; // 같은 단과대
        return 0; // 다른 단과대
    }
}