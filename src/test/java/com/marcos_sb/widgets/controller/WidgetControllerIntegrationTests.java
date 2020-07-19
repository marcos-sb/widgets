package com.marcos_sb.widgets.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcos_sb.widgets.resource.NewWidgetSpec;
import com.marcos_sb.widgets.resource.Widget;
import com.marcos_sb.widgets.resource.WidgetMutationSpec;
import com.marcos_sb.widgets.util.WidgetOps;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
class WidgetControllerIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	final NewWidgetSpec newWidgetSpecOk =
		new NewWidgetSpec(1L, 1L, 1.1D, 1.1D, 0);

	ResultActions create(NewWidgetSpec widgetSpec) throws Exception {
		return
			mockMvc.perform(post("/widgets/new")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(widgetSpec)));
	}

	@Nested
	@DisplayName("create widget")
	class Create {

		@Test
		@DisplayName("success")
		void createOneWidgetOk() throws Exception {
			final String responseBody =
				create(newWidgetSpecOk)
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString(StandardCharsets.UTF_8);

			// Cannot compare the returned json because of 'last-modified'
			final Widget created = objectMapper.readValue(responseBody, Widget.class);
			final Widget expected = WidgetOps.widgetFrom(
				created.getUUID(), newWidgetSpecOk, newWidgetSpecOk.getzIndex());

			assertEquals(expected, created);
		}

		@Test
		@DisplayName("bad request")
		void createOneWidgetBadRequest() throws Exception {
			final NewWidgetSpec newWidgetSpecKo =
				new NewWidgetSpec(1L, 1L, -1D, 1.1D, 0);

			create(newWidgetSpecKo).andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("after creating one widget")
	class AfterCreatingOneWidget {

		Widget created;

		@BeforeEach
		void createOneWidget() throws Exception {
			final String responseBody =
				create(newWidgetSpecOk)
					.andReturn()
					.getResponse()
					.getContentAsString(StandardCharsets.UTF_8);
			created = objectMapper.readValue(responseBody, Widget.class);
		}

		@Nested
		@DisplayName("get")
		class Get {
			@Test
			@DisplayName("existent widget")
			void getExistent() throws Exception {
				final String responseBody =
					mockMvc.perform(get("/widgets/{uuid}", created.getUUID())
						.contentType(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andReturn()
						.getResponse()
						.getContentAsString(StandardCharsets.UTF_8);

				final Widget actual = objectMapper.readValue(responseBody, Widget.class);
				assertEquals(created, actual);
			}

			@Test
			@DisplayName("nonexistent widget")
			void getNonExistent() throws Exception {
				mockMvc.perform(get("/widgets/{uuid}", UUID.randomUUID())
					.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isNotFound());
			}
		}

		@Nested
		@DisplayName("get all")
		class GetAll {

			@Test
			@DisplayName("response body is a list")
			void getAll() throws Exception {
				final String responseBody =
					mockMvc.perform(get("/widgets/list/all")
						.contentType(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andReturn()
						.getResponse()
						.getContentAsString(StandardCharsets.UTF_8);
				objectMapper.readValue(responseBody, List.class);
			}
		}

		@Nested
		@DisplayName("update")
		class Update {

			ResultActions update(WidgetMutationSpec mutationSpec) throws Exception {
				return
					mockMvc.perform(put("/widgets/update")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(mutationSpec)));
			}

			@Test
			@DisplayName("success full mutation")
			void updateFullMutationOk() throws Exception {
				final WidgetMutationSpec fullMutationSpec =
					new WidgetMutationSpec(created.getUUID(), 20L, 21L, 2.5D, 2.7D, 34);
				final Widget expected = WidgetOps.update(created, fullMutationSpec);

				final String responseBody =
					update(fullMutationSpec)
						.andExpect(status().isOk())
						.andReturn()
						.getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);
				final Widget updated = objectMapper.readValue(responseBody, Widget.class);

				assertEquals(expected, updated);
			}

			@Test
			@DisplayName("success identity mutation")
			void updateIdentityMutationOk() throws Exception {
				final WidgetMutationSpec identityMutationSpec =
					new WidgetMutationSpec(created.getUUID(), null, null, null, null, null);
				final Widget expected = WidgetOps.update(created, identityMutationSpec);

				final String responseBody =
					update(identityMutationSpec)
						.andExpect(status().isOk())
						.andReturn()
						.getResponse()
						.getContentAsString(StandardCharsets.UTF_8);
				final Widget updated = objectMapper.readValue(responseBody, Widget.class);

				assertEquals(expected, updated);
			}

			@Test
			@DisplayName("nonexistent widget")
			void updateNonExistent() throws Exception {
				final WidgetMutationSpec mutationSpec =
					new WidgetMutationSpec(UUID.randomUUID(), 20L, 21L, 2.5D, 2.7D, 34);

				update(mutationSpec).andExpect(status().isNotFound());
			}

			@Test
			@DisplayName("bad request")
			void updateBadRequest() throws Exception {
				final WidgetMutationSpec mutationSpecKo =
					new WidgetMutationSpec(UUID.randomUUID(), 20L, 21L, 2.5D, -2.7D, 34);

				update(mutationSpecKo).andExpect(status().isBadRequest());
			}
		}

		@Nested
		@DisplayName("delete")
		class Delete {

			@Test
			@DisplayName("existent widget")
			void deleteOk() throws Exception {
				final String responseBody =
					mockMvc.perform(delete("/widgets/delete/{uuid}", created.getUUID()))
						.andExpect(status().isOk())
						.andReturn()
						.getResponse()
						.getContentAsString(StandardCharsets.UTF_8);
				final Widget deleted = objectMapper.readValue(responseBody, Widget.class);
				assertEquals(created, deleted);
			}

			@Test
			@DisplayName("nonexistent widget")
			void deleteNonExistent() throws Exception {
				mockMvc.perform(delete("/widgets/delete/{uuid}", UUID.randomUUID()))
					.andExpect(status().isNotFound());
			}
		}
	}
}
