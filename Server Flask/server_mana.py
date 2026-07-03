from flask import Flask, request, jsonify
from flask_cors import CORS
import os

app = Flask(__name__)
CORS(app)

@app.route('/comanda', methods=['POST'])
def primeste_comanda():
    date = request.json
    id_deget = date.get('id_deget')
    unghi = date.get('valoare_unghi')

    # Rulează executabilul tău în C
    os.system(f"./mana {id_deget} {unghi}")
    
    return jsonify({"status": "ok"}), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
