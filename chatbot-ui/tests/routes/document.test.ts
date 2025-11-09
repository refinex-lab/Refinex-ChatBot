import type {Document} from "@/lib/db/schema";
import {getMessageByErrorCode} from "@/lib/errors";
import {generateUUID} from "@/lib/utils";
import {expect, test} from "../fixtures";

const documentsCreatedByAda: Document[] = [];

test.describe
  .serial("/api/document", () => {
    test("Ada 无法在没有指定 id 的情况下检索文档", async ({
      adaContext,
    }) => {
      const response = await adaContext.request.get("/api/document");
      expect(response.status()).toBe(400);

      const { code, message } = await response.json();
      expect(code).toEqual("bad_request:api");
      expect(message).toEqual(getMessageByErrorCode(code));
    });

    test("Ada 无法检索不存在的文档", async ({
      adaContext,
    }) => {
      const documentId = generateUUID();

      const response = await adaContext.request.get(
        `/api/document?id=${documentId}`
      );
      expect(response.status()).toBe(404);

      const { code, message } = await response.json();
      expect(code).toEqual("not_found:document");
      expect(message).toEqual(getMessageByErrorCode(code));
    });

    test("Ada 可以创建文档", async ({ adaContext }) => {
      const documentId = generateUUID();

      const draftDocument = {
        title: "Ada's Document",
        kind: "text",
        content: "Created by Ada",
      };

      const response = await adaContext.request.post(
        `/api/document?id=${documentId}`,
        {
          data: draftDocument,
        }
      );
      expect(response.status()).toBe(200);

      const [createdDocument] = await response.json();
      expect(createdDocument).toMatchObject(draftDocument);

      documentsCreatedByAda.push(createdDocument);
    });

    test("Ada 可以检索已创建的文档", async ({ adaContext }) => {
      const [document] = documentsCreatedByAda;

      const response = await adaContext.request.get(
        `/api/document?id=${document.id}`
      );
      expect(response.status()).toBe(200);

      const retrievedDocuments = await response.json();
      expect(retrievedDocuments).toHaveLength(1);

      const [retrievedDocument] = retrievedDocuments;
      expect(retrievedDocument).toMatchObject(document);
    });

    test("Ada 可以保存文档的新版本", async ({
      adaContext,
    }) => {
      const [firstDocument] = documentsCreatedByAda;

      const draftDocument = {
        title: "Ada's Document",
        kind: "text",
        content: "Updated by Ada",
      };

      const response = await adaContext.request.post(
        `/api/document?id=${firstDocument.id}`,
        {
          data: draftDocument,
        }
      );
      expect(response.status()).toBe(200);

      const [createdDocument] = await response.json();
      expect(createdDocument).toMatchObject(draftDocument);

      documentsCreatedByAda.push(createdDocument);
    });

    test("Ada 可以检索所有版本的文档", async ({
      adaContext,
    }) => {
      const [firstDocument, secondDocument] = documentsCreatedByAda;

      const response = await adaContext.request.get(
        `/api/document?id=${firstDocument.id}`
      );
      expect(response.status()).toBe(200);

      const retrievedDocuments = await response.json();
      expect(retrievedDocuments).toHaveLength(2);

      const [firstRetrievedDocument, secondRetrievedDocument] =
        retrievedDocuments;
      expect(firstRetrievedDocument).toMatchObject(firstDocument);
      expect(secondRetrievedDocument).toMatchObject(secondDocument);
    });

    test("Ada 无法在没有指定 id 的情况下删除文档", async ({
      adaContext,
    }) => {
      const response = await adaContext.request.delete("/api/document");
      expect(response.status()).toBe(400);

      const { code, message } = await response.json();
      expect(code).toEqual("bad_request:api");
      expect(message).toEqual(getMessageByErrorCode(code));
    });

    test("Ada 无法在没有指定时间戳的情况下删除文档", async ({
      adaContext,
    }) => {
      const [firstDocument] = documentsCreatedByAda;

      const response = await adaContext.request.delete(
        `/api/document?id=${firstDocument.id}`
      );
      expect(response.status()).toBe(400);

      const { code, message } = await response.json();
      expect(code).toEqual("bad_request:api");
      expect(message).toEqual(getMessageByErrorCode(code));
    });

    test("Ada 可以指定 id 和时间戳删除文档", async ({
      adaContext,
    }) => {
      const [firstDocument, secondDocument] = documentsCreatedByAda;

      const response = await adaContext.request.delete(
        `/api/document?id=${firstDocument.id}&timestamp=${firstDocument.createdAt}`
      );
      expect(response.status()).toBe(200);

      const deletedDocuments = await response.json();
      expect(deletedDocuments).toHaveLength(1);

      const [deletedDocument] = deletedDocuments;
      expect(deletedDocument).toMatchObject(secondDocument);
    });

    test("Ada 可以检索没有删除版本的文档", async ({
      adaContext,
    }) => {
      const [firstDocument] = documentsCreatedByAda;

      const response = await adaContext.request.get(
        `/api/document?id=${firstDocument.id}`
      );
      expect(response.status()).toBe(200);

      const retrievedDocuments = await response.json();
      expect(retrievedDocuments).toHaveLength(1);

      const [firstRetrievedDocument] = retrievedDocuments;
      expect(firstRetrievedDocument).toMatchObject(firstDocument);
    });

    test("Babbage 无法更新 Ada 的文档", async ({ babbageContext }) => {
      const [firstDocument] = documentsCreatedByAda;

      const draftDocument = {
        title: "Babbage's Document",
        kind: "text",
        content: "Created by Babbage",
      };

      const response = await babbageContext.request.post(
        `/api/document?id=${firstDocument.id}`,
        {
          data: draftDocument,
        }
      );
      expect(response.status()).toBe(403);

      const { code, message } = await response.json();
      expect(code).toEqual("forbidden:document");
      expect(message).toEqual(getMessageByErrorCode(code));
    });

    test("Ada 的文档没有更新", async ({ adaContext }) => {
      const [firstDocument] = documentsCreatedByAda;

      const response = await adaContext.request.get(
        `/api/document?id=${firstDocument.id}`
      );
      expect(response.status()).toBe(200);

      const documentsRetrieved = await response.json();
      expect(documentsRetrieved).toHaveLength(1);
    });
  });
