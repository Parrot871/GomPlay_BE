package com.example.gomplay.domain.survey.service;

import com.example.gomplay.domain.survey.dto.ReportResponse;
import com.example.gomplay.domain.survey.entity.UserSurvey;
import com.example.gomplay.domain.survey.entity.UserSurveyExercise;
import com.example.gomplay.domain.survey.repository.UserSurveyExerciseRepository;
import com.example.gomplay.domain.survey.repository.UserSurveyRepository;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserSurveyRepository userSurveyRepository;
    private final UserSurveyExerciseRepository userSurveyExerciseRepository;
    private final UserProfileRepository userProfileRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // 1. 설정 파일(application.yml)에서 읽어올 키 이름을 groq로 변경합니다.
    @Value("${groq.api.key}")
    private String groqApiKey;

    // 2. Groq API 엔드포인트 URL로 변경합니다.
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    @Transactional(readOnly = true)
    public ReportResponse generateReport(Long userId) {
        UserProfile userProfile = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        UserSurvey survey = userSurveyRepository.findByUserProfile_Id(userProfile.getId())
                .orElseThrow(() -> new IllegalArgumentException("설문을 찾을 수 없습니다."));

        List<UserSurveyExercise> exercises = userSurveyExerciseRepository
                .findByUserProfile_Id(userProfile.getId());

        String prompt = buildPrompt(survey, exercises);
        String jsonResult = callGroq(prompt); // 메서드명 변경

        return parseReport(userProfile.getId(), survey, exercises, jsonResult);
    }

    private String mapSummary(String personalityType, String intensityType, String purposeType) {
        // 기존 코드와 동일하여 생략 없이 유지합니다.
        String key = personalityType + "+" + intensityType + "+" + purposeType;
        return switch (key) {
            case "독립형+여유형+힐링 추구"  -> "혼자 조용히, 느긋하게 몸과 마음을 충전하는 스타일이에요!";
            case "독립형+여유형+관계 중심"  -> "같이 있어도 각자 페이스로, 가볍게 땀 흘리며 친해지는 스타일이에요!";
            case "독립형+여유형+실력 향상"  -> "혼자 묵묵히, 부담 없이 조금씩 실력을 쌓아가는 스타일이에요!";
            case "독립형+여유형+건강 관리"  -> "무리 없이 꾸준하게, 내 몸을 챙기는 걸 즐기는 스타일이에요!";
            case "독립형+균형형+힐링 추구"  -> "혼자 적당히 땀 흘리며 스트레스를 날려버리는 스타일이에요!";
            case "독립형+균형형+관계 중심"  -> "각자 운동하면서도 자연스럽게 유대감을 쌓아가는 스타일이에요!";
            case "독립형+균형형+실력 향상"  -> "혼자 집중하면서 균형 잡힌 성장을 추구하는 스타일이에요!";
            case "독립형+균형형+건강 관리"  -> "적당한 강도로 꾸준히, 건강한 루틴을 만들어가는 스타일이에요!";
            case "독립형+집중형+힐링 추구"  -> "혼자 몰입하며 운동으로 머릿속을 완전히 비워버리는 스타일이에요!";
            case "독립형+집중형+관계 중심"  -> "말 없이 옆에서 함께 집중하는 것만으로도 충분한 스타일이에요!";
            case "독립형+집중형+실력 향상"  -> "혼자 깊이 파고들며 실력을 끌어올리는 걸 즐기는 스타일이에요!";
            case "독립형+집중형+건강 관리"  -> "제대로 집중해서 몸을 단단하게 만들어가는 스타일이에요!";
            case "독립형+도전형+힐링 추구"  -> "혼자 한계까지 밀어붙이며 스트레스를 통쾌하게 날리는 스타일이에요!";
            case "독립형+도전형+관계 중심"  -> "말 없이 함께 한계를 넘으며 진짜 유대감을 만드는 스타일이에요!";
            case "독립형+도전형+실력 향상"  -> "혼자서 끝까지 밀어붙이며 한 단계씩 성장하는 스타일이에요!";
            case "독립형+도전형+건강 관리"  -> "한계까지 도전하며 몸을 철저하게 관리하는 스타일이에요!";
            case "소통형+여유형+힐링 추구"  -> "수다 떨며 가볍게 땀 흘리는 게 최고의 힐링인 스타일이에요!";
            case "소통형+여유형+관계 중심"  -> "운동보다 대화가 더 즐거운, 친목이 먼저인 스타일이에요!";
            case "소통형+여유형+실력 향상"  -> "같이 이야기하면서 느긋하게 실력도 키워가는 스타일이에요!";
            case "소통형+여유형+건강 관리"  -> "편하게 대화하며 건강도 챙기는 일석이조 스타일이에요!";
            case "소통형+균형형+힐링 추구"  -> "같이 운동하며 떠들다 보면 스트레스가 싹 날아가는 스타일이에요!";
            case "소통형+균형형+관계 중심"  -> "적당히 운동하면서 사람과의 시간을 제일 소중히 여기는 스타일이에요!";
            case "소통형+균형형+실력 향상"  -> "함께 으쌰으쌰하며 균형 있게 실력을 키워가는 스타일이에요!";
            case "소통형+균형형+건강 관리"  -> "대화하며 적당히 움직이고, 건강하게 일상을 채워가는 스타일이에요!";
            case "소통형+집중형+힐링 추구"  -> "같이 제대로 땀 흘리고 나서 후련함을 나누는 스타일이에요!";
            case "소통형+집중형+관계 중심"  -> "함께 열심히 하면서 더 깊이 친해지는 걸 즐기는 스타일이에요!";
            case "소통형+집중형+실력 향상"  -> "서로 자극 주고받으며 제대로 성장하는 걸 즐기는 스타일이에요!";
            case "소통형+집중형+건강 관리"  -> "같이 집중해서 운동하며 서로의 건강을 응원하는 스타일이에요!";
            case "소통형+도전형+힐링 추구"  -> "같이 한계까지 불태우고 나서 후련함을 함께 나누는 스타일이에요!";
            case "소통형+도전형+관계 중심"  -> "함께 극한까지 도전하며 끈끈한 전우애를 쌓는 스타일이에요!";
            case "소통형+도전형+실력 향상"  -> "서로 밀어주며 한계를 돌파하는 성장 지향 스타일이에요!";
            case "소통형+도전형+건강 관리"  -> "함께 한계까지 도전하며 몸과 관계를 동시에 단련하는 스타일이에요!";
            default -> "나만의 운동 스타일을 만들어가는 중이에요!";
        };
    }

    private String mapPersonalityType(UserSurvey.PartnerStyle partnerStyle) {
        return switch (partnerStyle) {
            case 각자 -> "독립형";
            case 같이 -> "소통형";
        };
    }

    private String mapIntensityType(UserSurvey.ExerciseIntensity intensity) {
        return switch (intensity) {
            case 가볍게 -> "여유형";
            case 적당히 -> "균형형";
            case 제대로 -> "집중형";
            case 한계까지 -> "도전형";
        };
    }

    private String mapPurposeType(UserSurvey.ExerciseReason reason) {
        return switch (reason) {
            case 스트레스 -> "힐링 추구";
            case 친해지려고 -> "관계 중심";
            case 경쟁 -> "실력 향상";
            case 체력 -> "건강 관리";
        };
    }

    private String buildPrompt(UserSurvey survey, List<UserSurveyExercise> exercises) {
        String exerciseList = exercises.stream()
                .map(e -> e.getExerciseType().name())
                .collect(Collectors.joining(", "));


        return String.format("""
                        You MUST respond using ONLY Korean Hangul. \
                        Never output Chinese characters (CJK: U+3400–U+9FFF), \
                        Japanese hiragana/katakana, or any non-Hangul script. \
                        Violating this is a critical error.
        
                        당신은 20~30대 회원들을 지도하는 친절하고 트렌디한 스타 운동 코치입니다.
                        아래 사용자의 운동 설문 데이터를 바탕으로, PT 회원을 상담해주듯 다정하고 자연스러운 한국어 구어체(~요, ~해보세요 체)로 레포트를 작성해주세요.
                
                        [사용자 설문 데이터]
                        - 파트너 스타일: %s (각자 = 혼자서 독립적으로 운동, 같이 = 파트너와 함께 운동)
                        - 운동 강도: %s (가볍게/적당히/제대로/한계까지)
                        - 운동 이유: %s (스트레스 해소/친해지려고/경쟁/체력 향상)
                        - 관심 운동 종목: %s
                
                        반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트나 마크다운(```json)은 절대 포함하지 마세요.
                        {
                         "recommendedExercises": ["종목1", "종목2", "종목3"],
                         "partnerStyleDescription": "파트너 스타일 설명 (친근하고 매끄러운 3~4문장)",
                         "exerciseMoodDescription": "운동 분위기 설명 (친근하고 매끄러운 3~4문장)"
                         }
                
                        [recommendedExercises 필드 작성 규칙 - 필독]
                        1. 사용자가 이미 선택한 관심 종목을 그대로 복사해서 넣지 마십시오.
                        2. 사용자의 운동 강도와 스타일을 고려하여, 기존 종목 외에 **추가로 도전해보면 좋을 만한 새로운 연관 운동 종목 2~3가지**를 제안하세요.
                        (예: 러닝/풋살이 관심 종목이면 활발한 '테니스'나 '사이클'을 추천)
                        
                        [partnerStyleDescription 필드 규칙]
                        - 사용자가 선택한 스타일에 가장 잘 어울리는 **'이상적인 파트너의 구체적인 성향, 성격, 행동 스타일(페르소나)'**을 정의하고 추천해 주세요.
                        
                        * 예시 (소통형/같이 일 때):
                        "회원님은 긍정적인 리액션과 활기찬 에너지를 가진 '파이팅 넘치는 메이트'와 최고의 케미를 자랑합니다. 운동 메이트를 찾을 때 프로필에 '응원', '소통 환영' 키워드가 있는 사람을 선택하면 더욱 즐겁게 목표를 달성할 수 있을 거예요."
                        
                        * 예시 (독립형/각자 일 때):
                        "회원님은 각자의 루틴에 온전히 집중하며 묵묵히 자리를 지켜주는 '매너 집중형 메이트'와 잘 맞습니다. 과도한 대화보다는 정해진 시간에 출석을 인증하고 서로 눈인사만 가볍게 나눈 뒤, 개인 운동에 몰입할 수 있는 파트너를 추천합니다."
                        
                        [exerciseMoodDescription 필드 작성 규칙]
                        - 사용자의 성향과 강도에 맞는 **'최적의 운동 공간(장소), 음악 스타일, 조명, 주변 소음 레벨(캐주얼함 vs 엄숙함)'**을 구체적으로 묘사하세요.
                        
                        * 예시 (적당한 강도 + 소통형 일 때):
                        "회원님은 트렌디한 음악이 흘러나오는 밝고 활기찬 피트니스 센터나, 가벼운 대화를 나누며 달릴 수 있는 탁 트인 저녁 공원 같은 캐주얼한 분위기가 딱 맞아요. 너무 무겁고 조용한 공간보다는 적당한 소음과 활력이 도는 환경에서 에너지가 더 잘 생기는 스타일입니다."
                        
                        * 예시 (한계까지 + 독립형 일 때):
                        "회원님은 조명이 다소 어둡고 묵직한 힙합이나 일렉트로닉 비트가 깔린, 오롯이 거친 숨소리와 쇳소리에만 몰입할 수 있는 하드코어한 분위기에서 최고의 효율을 냅니다. 타인의 시선이 차단되고 모두가 각자의 성장에만 집중하는 엄숙한 분위기의 센터를 추천합니다."
                        
                        [★ 절대 금지 및 준수 사항 ★]
                        1. 한자(예: 户외, 生活, 運動 등)는 단 한 글자도 쓰지 마세요. 100%% 순수한 한글로만 작성해야 합니다.
                        2. 영어, 일본어, 중국어 단어가 섞이면 절대 안 됩니다. 오직 한글, 숫자, 문장부호만 허용합니다.
                        3. "~할 것이다", "~일 것이다" 같은 번역기 같은 딱딱한 말투는 절대 금지합니다. 대화하듯 부드러운 "~요", "~죠", "~해 보세요" 말투를 사용하세요.
                        4. "~것 같아요", "~될 거예요", "~있을 거예요" 같은 추측성 표현은 쓰지 마세요. "~해 보세요", "~추천드려요", "~잘 맞아요", "~도움이 돼요" 처럼 확신 있는 표현을 쓰세요.
                        
                        [말투 예시 - 반드시 참고하세요]
                        ❌나쁜 예: "함께하면 더 효율적인 운동이 될 수 있을 거예요"
                        ✅좋은 예: "함께하면 운동 효과가 훨씬 올라가요!"
                        ❌나쁜 예: "잘 맞을 것 같아요"
                        ✅좋은 예: "딱 맞아요!", "강력 추천이에요!"
                        ❌나쁜 예: "도움이 될 거예요"
                        ✅좋은 예: "확실히 도움이 돼요", "효과 있어요"
                        조건:
                        - recommendedExercises는 관심 종목을 참고하되, 설문 데이터 전체를 고려해 최적의 3가지 종목을 추천하세요.
                        - partnerStyleDescription은 이 유저에게 잘 맞는 파트너 유형을 구체적으로 설명하세요.
                        - exerciseMoodDescription은 이 유저에게 맞는 운동 분위기와 환경을 설명하세요.
                        """,
                survey.getPartnerStyle().name(),
                survey.getExerciseIntensity().name(),
                survey.getExerciseReason().name(),
                exerciseList
        );
    }

    // 3. Groq API 호출 규격에 맞게 메서드를 수정합니다.
    private String callGroq(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Groq(OpenAI 호환)는 헤더에 Bearer 토큰 형식으로 키를 넣습니다.
        headers.setBearerAuth(groqApiKey);

        // 요청 Body 구조를 Groq(OpenAI) 형식으로 변경합니다.
        Map<String, Object> body = Map.of(
                "model", "openai/gpt-oss-120b",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7,
                "response_format", Map.of("type", "json_object") // JSON 반환 강제 (Groq 지원 기능)
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    GROQ_URL, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());

            String raw = root.path("choices").get(0)
                    .path("message")
                    .path("content").asText();

            // 마크다운 제거
            String cleaned = raw.replaceAll("(?s)```json\\s*|```", "").trim();

            // ✅ 여기! 한자/일본어 감지 후 예외 처리
            if (containsNonKorean(cleaned)) {
                throw new RuntimeException("비한글 문자 포함 응답 감지");
            }

            return cleaned;

        } catch (Exception e) {
            throw new RuntimeException("Groq API 호출 실패: " + e.getMessage());
        }
    }
    private boolean containsNonKorean(String text) {
        return text.chars().anyMatch(c ->
                (c >= 0x3000 && c <= 0x303F) ||  // CJK 기호 및 구두점
                        (c >= 0x3040 && c <= 0x30FF) ||  // 히라가나 / 가타카나
                        (c >= 0x3400 && c <= 0x4DBF) ||  // CJK 확장 A
                        (c >= 0x4E00 && c <= 0x9FFF)     // 한자 (가장 흔한 범위)
        );
    }

    private ReportResponse parseReport(Long userId, UserSurvey survey, List<UserSurveyExercise> exercises, String jsonResult) {
        try {
            JsonNode node = objectMapper.readTree(jsonResult);

            List<String> recommendedExercises = objectMapper.convertValue(
                    node.path("recommendedExercises"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );

            String personalityType = mapPersonalityType(survey.getPartnerStyle());
            String intensityType   = mapIntensityType(survey.getExerciseIntensity());
            String purposeType     = mapPurposeType(survey.getExerciseReason());

            List<String> exerciseTypes = exercises.stream()
                    .map(e -> e.getExerciseType().name())
                    .collect(Collectors.toList());

            return ReportResponse.builder()
                    .userId(userId)
                    .personalityType(personalityType)
                    .intensityType(intensityType)
                    .purposeType(purposeType)
                    .summary(mapSummary(personalityType, intensityType, purposeType))
                    .exerciseTypes(exerciseTypes)
                    .recommendedExercises(recommendedExercises)
                    .partnerStyleDescription(node.path("partnerStyleDescription").asText())
                    .exerciseMoodDescription(node.path("exerciseMoodDescription").asText())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("레포트 파싱 실패: " + e.getMessage());
        }
    }
}