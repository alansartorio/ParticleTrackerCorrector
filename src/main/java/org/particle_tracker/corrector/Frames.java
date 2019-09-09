package org.particle_tracker.corrector;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.csv.*;

class FramesData {

    Frame[] frames;

    public FramesData(int frameCount) {
        frames = new Frame[frameCount];
        initializeFrames();
    }

    public final void initializeFrames() {
        for (int i = 0; i < frames.length; i++) {
            frames[i] = new Frame();
        }
    }

    public void setFrameCount(int frameCount) {
        Frame[] newFrames = new Frame[frameCount];
        for (int i = 0; i < newFrames.length; i++) {
            if (i < frames.length) {
                newFrames[i] = frames[i];
            } else {
                newFrames[i] = new Frame();
            }
        }
        frames = newFrames;
    }

    public void saveToCSV(File file) throws IOException {
        CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(new FileOutputStream(file)), CSVFormat.DEFAULT);
        for (int frame = 0; frame < frames.length; frame++) {
            for (Particle particle : frames[frame].particles) {
                csvPrinter.printRecord(frame + 1, particle.position.y, particle.position.x, particle.identity.id);
            }
        }
        csvPrinter.close();
    }

    public void loadFromCSV(File file) throws IOException, ArrayIndexOutOfBoundsException {
        initializeFrames();

        CSVParser csvParser = CSVFormat.DEFAULT.parse(new InputStreamReader(new FileInputStream(file)));
        for (CSVRecord record : csvParser) {
            int frame = Integer.valueOf(record.get(0)) - 1;
            int y = Integer.valueOf(record.get(1));
            int x = Integer.valueOf(record.get(2));
            int id = Integer.valueOf(record.get(3));

            if (frame >= frames.length) {
                throw new ArrayIndexOutOfBoundsException("El archivo CSV contiene mas frames que el video");
            }
            if (id != -1) {
                frames[frame].addParticle(new Particle(new Point(x, y), createIdentity(id)));
            } else {
                frames[frame].addParticle(new Particle(new Point(x, y)));
            }
        }

    }

//Reasigna todos los numeros de ID para reducir la magnitud de estos
    void compactIds() {
        Set<Identity> reasigned = new HashSet<>();
        for (Frame frame : frames) {
            for (Particle particle : frame.particles) {
                if (!reasigned.contains(particle.identity)) {
                    particle.identity.id = reasigned.size();
                    reasigned.add(particle.identity);
                }
            }
        }
        Identity.nextIdentityId = reasigned.size();
    }

    //Retorna una instancia existente con id igual al parametro id en caso de que exita. De caso contrario, se crea una nueva instancia
    Identity createIdentity(int id) {
        for (Frame frame : frames) {
            for (Particle particle : frame.particles) {
                if (particle.identity.id == id) {
                    return particle.identity;
                }
            }
        }
        return new Identity(id);
    }

    //Retorna todas las particulas que tienen identidad 'identity' de todos los frames
    Particle[] getParticlesByIdentity(Identity identity) {
        ArrayList<Particle> particles = new ArrayList<>();
        for (Frame frame : frames) {
            Particle particle = frame.searchByIdentity(identity);
            if (particle != null) {
                particles.add(particle);
            }
        }

        return particles.toArray(new Particle[particles.size()]);
    }
}
