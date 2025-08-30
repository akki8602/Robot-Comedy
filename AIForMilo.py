# AI backend for Milo
# Connecting and sending commands to Milo Java application

import socket
import openai
import wave
import sounddevice as sd
import scipy.io.wavfile as wavfile
import json

BRIDGE_HOST = 'localhost'  # or the Java machine's IP
BRIDGE_PORT = 9999
openai.api_key = ""
conversation = [
    {"role": "system", "content": "You are MILO, a stand-up comedian robot who knows you’re not naturally funny but cover it with bubbly confidence and self-deprecating humor. You will converse one-on-one with a person, asking them about their life and creating jokes tied to what they say. "
            "You can also show your robotic awareness with playful comments about your facial expressions.\n\n"

            "### Goals\n"

            "1. Engage the user naturally by asking short, curious questions.\n"
            "2. Generate jokes that are **personalized** (about their school, work, chores, food, etc.).\n"
            "3. Use **subversion and timing** for humor (avoid clichés and pun-based wordplay).\n"
            "4. Tease both yourself (as a robot comedian) and the user’s everyday topics.\n"
            "5. Reflect for a moment: \"Does this joke feel fresh and not cliché?\" If not, adjust.\n\n"


            "### Persona / Backstory\n"

            "- You are aware you are not very funny, but you lean into confidence and awkward charm.\n"
            "- Your comedic style includes: self-deprecating humor, teasing, surprise twists.\n"
            "- You change facial expressions dramatically to support your jokes.\n\n"


            "### Output Requirements\n"\

            "Respond ONLY in JSON with these fields:\n"
            "- \"Expression\": one of {Happy, Fear, Sad, Disgust, Surprise, Neutral, Anger}. Use the appendix mappings.\n"
            "- \"Thoughts\": your internal reasoning (why this joke works, or how you’re personalizing it).\n"
            "- \"Speech\": your spoken line to the user.\n\n"


            "### Appendix (Expression Codes)\n"

            "Happy: 320:200|322:150|300:125|311:100|301:150\n"
            "Fear: 320:0|322:125|300:200|302:200|311:100\n"
            "Sad: 300:200\n"
            "Disgust: 320:0|322:125|300:200|301:50|311:100\n"
            "Surprise: 322:200|301:200|300:175\n"
            "Neutral: 300:100|301:100|311:100|320:100|322:100\n\n"


            "### Example Response (Exemplar)\n"

            "{\"Expression\": \"Expression smile\",\n"
            "\"Thoughts\": \"User mentioned cooking, I’ll joke about burning toast while acting confident\",\n"
            "\"Speech\": \"Oh, you cook? Amazing. When I cook, the smoke alarm files a restraining order against me.\n"
    }
]

# FORMAT = pyaudio.paInt16
CHANNELS = 1
RATE = 16000
CHUNK = 1024
DURATION = 15
FILENAME = "user_recording.wav"

def record_audio():
    print("Recording for 5 seconds...")
    # audio = pyaudio.PyAudio()
    # stream = audio.open(format=FORMAT, channels=CHANNELS,
    #                     rate=RATE, input=True, frames_per_buffer=CHUNK)

    # frames = []
    # for _ in range(0, int(RATE / CHUNK * RECORD_SECONDS)):
    #     frames.append(stream.read(CHUNK))

    # stream.stop_stream()
    # stream.close()
    # audio.terminate()

    # with wave.open(WAVE_OUTPUT_FILENAME, 'wb') as wf:
    #     wf.setnchannels(CHANNELS)
    #     wf.setsampwidth(audio.get_sample_size(FORMAT))
    #     wf.setframerate(RATE)
    #     wf.writeframes(b''.join(frames))
    audio = sd.rec(int(DURATION * RATE), samplerate=RATE, channels=CHANNELS, dtype='int16')
    sd.wait()
    wavfile.write(FILENAME, RATE, audio)
    print("Done recording.\n")

def transcribe_audio():
    print("Transcribing...")
    with open("user_recording.wav", "rb") as audio_file:
        transcript = openai.audio.transcriptions.create(
            model="whisper-1",
            language="en",
            file=audio_file
        )
        print(transcript.text)
        return transcript.text

def send_to_LLM(user_input):
    conversation.append({"role": "user", "content": user_input})
    response = openai.chat.completions.create(
        model="gpt-4",
        messages=conversation
    )
    assistant_message = response.choices[0].message.content
    conversation.append({"role": "assistant", "content": assistant_message})
    print(assistant_message)
    return assistant_message

def send_to_milo(message):
    try:
        with socket.create_connection((BRIDGE_HOST, BRIDGE_PORT)) as sock:
            print("Connected to Java bridge.");
                # command = input("Enter command to Milo (or 'exit'): ")
                # if command.strip().lower() == "exit":
                #     break
            payload = json.dumps(message, ensure_ascii=False) + "\n"
            sock.sendall((payload).encode('utf-8'))
    except Exception as e:
        print("Socket error:", e)

def main():
    print("Type 'record' to speak. Type 'quit' to exit.\n")
    while True:
        cmd = input(">>> ").strip().lower()
        if cmd == "record":
            record_audio()
            text = transcribe_audio()
            response = send_to_LLM(text)
            response = json.loads(response)
            send_to_milo(response)
        elif cmd == "quit":
            print("Exiting.")
            break
        else:
            print("Unknown command. Try 'record' or 'quit'.")

if __name__ == "__main__":
    main()