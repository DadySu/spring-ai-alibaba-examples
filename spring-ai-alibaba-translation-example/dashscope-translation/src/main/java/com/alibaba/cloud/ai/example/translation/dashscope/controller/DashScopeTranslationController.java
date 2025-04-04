/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.ai.example.translation.dashscope.controller;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import jakarta.servlet.http.HttpServletResponse;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Makoto
 * @author <a href="mailto:makoto@example.com">Makoto</a>
 */

@RestController
@RequestMapping("/translation")
public class DashScopeTranslationController {

	private static final String TRANSLATION_PROMPT_TEMPLATE = "请将以下文本从%s翻译成%s：\n\n%s";

	private final ChatModel dashScopeChatModel;

	public DashScopeTranslationController(ChatModel chatModel) {
		this.dashScopeChatModel = chatModel;
	}

	/**
	 * 基础翻译服务
	 * @param text 需要翻译的文本
	 * @param sourceLanguage 源语言
	 * @param targetLanguage 目标语言
	 * @return 翻译后的文本
	 */
	@GetMapping("/simple")
	public String simpleTranslation(
			@RequestParam String text,
			@RequestParam(defaultValue = "中文") String sourceLanguage,
			@RequestParam(defaultValue = "英文") String targetLanguage) {

		String prompt = String.format(TRANSLATION_PROMPT_TEMPLATE, sourceLanguage, targetLanguage, text);
		
		return dashScopeChatModel.call(new Prompt(prompt, DashScopeChatOptions
				.builder()
				.withModel(DashScopeApi.ChatModel.QWEN_PLUS.getModel())
				.build())).getResult().getOutput().getText();
	}

	/**
	 * 流式翻译服务
	 * @param text 需要翻译的文本
	 * @param sourceLanguage 源语言
	 * @param targetLanguage 目标语言
	 * @return 翻译后的文本流
	 */
	@GetMapping("/stream")
	public Flux<String> streamTranslation(
			HttpServletResponse response,
			@RequestParam String text,
			@RequestParam(defaultValue = "中文") String sourceLanguage,
			@RequestParam(defaultValue = "英文") String targetLanguage) {

		// 避免返回乱码
		response.setCharacterEncoding("UTF-8");

		String prompt = String.format(TRANSLATION_PROMPT_TEMPLATE, sourceLanguage, targetLanguage, text);
		
		Flux<ChatResponse> stream = dashScopeChatModel.stream(new Prompt(prompt, DashScopeChatOptions
				.builder()
				.withModel(DashScopeApi.ChatModel.QWEN_PLUS.getModel())
				.build()));
		return stream.map(resp -> resp.getResult().getOutput().getText());
	}

	/**
	 * 使用自定义配置的翻译服务
	 * @param text 需要翻译的文本
	 * @param sourceLanguage 源语言
	 * @param targetLanguage 目标语言
	 * @return 翻译后的文本
	 */
	@GetMapping("/custom")
	public String customTranslation(
			@RequestParam String text,
			@RequestParam(defaultValue = "中文") String sourceLanguage,
			@RequestParam(defaultValue = "英文") String targetLanguage) {

		String prompt = String.format(TRANSLATION_PROMPT_TEMPLATE, sourceLanguage, targetLanguage, text);
		
		DashScopeChatOptions customOptions = DashScopeChatOptions.builder()
				.withModel(DashScopeApi.ChatModel.QWEN_PLUS.getModel())
				.withTopP(0.7)
				.withTopK(50)
				.withTemperature(0.5) // 降低温度以获得更准确的翻译
				.build();

		return dashScopeChatModel.call(new Prompt(prompt, customOptions)).getResult().getOutput().getText();
	}
} 