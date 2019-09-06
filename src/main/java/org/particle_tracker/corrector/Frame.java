package org.particle_tracker.corrector;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

public class Frame {
    ArrayList<Particle> particles = new ArrayList<>();

    public void draw(Graphics2D g) {
        for (Particle particle : particles) {
            particle.draw(g);
        }
        for (Particle particle : particles) {
            particle.drawIdentity(g);
        }
    }
    public void drawDashed(Graphics2D g) {
        for (Particle particle : particles) {
            particle.drawDashed(g);
        }
        for (Particle particle : particles) {
            particle.drawIdentity(g);
        }
    }

    public void addParticle(Particle particle) {
        particles.add(particle);
    }

    public void removeParticle(Particle particle) {
        particles.remove(particle);
    }
    
    public Particle searchByIdentity(Identity identity) {
    	for (Particle particle : particles) {
    		if (particle.identity == identity) {
    			return particle;
    		}
    	}
    	return null;
    }

    public Particle getParticleInPosition(Point position) {
        Particle closestParticle = null;
        Double closestDistance = null;
        for (Particle particle : particles) {
          double distance = particle.position.distance(position);//dist(particula.posicion.x, particula.posicion.y, x, y);
          if (distance < Particle.dragRadius && (closestDistance == null || distance < closestDistance))
          {
            closestParticle = particle;
            closestDistance = distance;
          }
        }
        return closestParticle;
    }
}