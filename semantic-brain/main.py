from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer, CrossEncoder
from typing import List, Dict, Any
import uvicorn
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Ecommerce Semantic Brain")

print("Loading embedding model...")
embedding_model = SentenceTransformer("paraphrase-multilingual-MiniLM-L12-v2")
print("Loading reranker...")
reranker = CrossEncoder("BAAI/bge-reranker-v2-m3")
print("AI Ready")

class QueryRequest(BaseModel):
    text: str

class RerankRequest(BaseModel):
    query: str
    candidates: List[Dict[str, Any]]
    category_id: int = None

@app.get("/health")
def health():
    return {"status": "UP"}

@app.post("/api/v1/embed")
def get_embedding(request: QueryRequest):
    try:
        vector = embedding_model.encode(request.text, normalize_embeddings=True)
        return {"vector": vector.tolist()}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/v1/rerank")
def rerank(request: RerankRequest):
    try:
        if not request.candidates:
            return []

        pairs = [[request.query, f"{c.get('productName', '')} {c.get('description', '')[:200]}"]
                 for c in request.candidates]

        scores = reranker.predict(pairs, batch_size=32)
        for i, candidate in enumerate(request.candidates):
            logger.info(f"Sản phẩm: {candidate.get('productName')} | Score: {scores[i]}")

        results = []
        for i, candidate in enumerate(request.candidates):
            item = dict(candidate)
            item["rerankScore"] = float(scores[i])
            results.append(item)

        results.sort(key=lambda x: x["rerankScore"], reverse=True)

        final_results = [r for r in results if r["rerankScore"] > 0.1]

        return final_results

    except Exception as e:
        logger.error(f"Rerank Error: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))
if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8000)